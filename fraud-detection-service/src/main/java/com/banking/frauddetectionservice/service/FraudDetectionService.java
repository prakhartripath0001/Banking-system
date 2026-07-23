package com.banking.frauddetectionservice.service;

import org.bouncycastle.jcajce.provider.asymmetric.dsa.DSASigner.detDSA;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.banking.frauddetectionservice.client.AccountServiceClient;
import com.banking.frauddetectionservice.model.FraudCheckResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudDetectionService {

    private final AccountServiceClient accountServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${fraud.max-transaction-per-minute}")
    private int maxTransactionPerMinute;
    @Value("${fraud.avg-amount-multiplier}")
    private double suspiciousAmountMultiplier;
    @Value("${fraud.max-balance-percentage}")
    private double maxBalancePercentage;

    private static final String VERIFICATION_REQUIRED_TOPIC = "verification.required";
    private static final String FRAUD_CHECK_CLEAN_RESULT = "fraud.check.clean.result";

    // Velocity check: tracks timestamps of recent transactions per account
    @Value("${fraud.max-transaction-per-minute}")
    private int velocityLimit;
    private static final long VELOCITY_WINDOW_SECONDS = 60;
    private final Map<String, List<Instant>> transactionTimestamps = new ConcurrentHashMap<>();

    // Amount check: suspicious if amount exceeds this threshold
    private static final BigDecimal SUSPICIOUS_AMOUNT_THRESHOLD = new BigDecimal("50000");

    // Balance check: suspicious if transaction drains more than 90% of balance
    private static final BigDecimal BALANCE_DRAIN_THRESHOLD = new BigDecimal("0.90");

    public void checkTransaction(Map<String, String> payload) {
        String transactionId = (String) payload.get("transactionId");
        String accountNumber = (String) payload.get("senderAccountNumber");
        String amount = (String) payload.get("amount");

        BigDecimal senderBalance = accountServiceClient.getBalance(accountNumber);

        if (senderBalance == null) {
            log.error("Failed to fetch sender balance for transaction {}", transactionId);
            return;
        }

        BigDecimal transactionAmount = new BigDecimal(amount);

        if (senderBalance.compareTo(transactionAmount) < 0) {
            log.error("Sender balance is less than transaction amount for transaction {}", transactionId);
            return;
        }

        log.info("Checking transaction : {} account {} amount{} balance{}", transactionId, accountNumber, amount,
                senderBalance);

        FraudCheckResult result = performFraudChecks(accountNumber, transactionAmount, senderBalance);

        if (result.isFraud()) {
            log.info("suspicious activity detected - account: {}" + "reason: {} - requesting OTP verification ",
                    accountNumber, result.getReason());

            Map<String, Object> verificationEvent = new HashMap<>();
            verificationEvent.put("transactionId", transactionId);
            verificationEvent.put("accountNumber", accountNumber);
            verificationEvent.put("amount", amount);
            verificationEvent.put("reason", result.getReason());

            kafkaTemplate.send(VERIFICATION_REQUIRED_TOPIC, transactionId, verificationEvent);
        } else {
            log.info("Transaction {} passed fraud check", transactionId);

            Map<String, Object> transactionCleanEvent = new HashMap<>();
            transactionCleanEvent.put("transactionId", transactionId);
            transactionCleanEvent.put("isFraud", false);
            transactionCleanEvent.put("reasong", null);

            kafkaTemplate.send(FRAUD_CHECK_CLEAN_RESULT, transactionId, transactionCleanEvent);
        }
    }

    private FraudCheckResult performFraudChecks(String accountNumber, BigDecimal amount, BigDecimal senderBalance) {
        if (isVelocityExceeded(accountNumber)) {
            return new FraudCheckResult(true, "Velocity limit exceeded: too many transactions in 60 seconds");
        }

        if (isAmountSuspicious(amount)) {
            return new FraudCheckResult(true, "Suspicious amount: transaction exceeds threshold of 50,000");
        }

        if (isBalanceCheckFailed(senderBalance, amount)) {
            return new FraudCheckResult(true, "Transaction exceeds 90% of account balance");
        }

        return new FraudCheckResult(false, null);
    }

    /**
     * Checks if the account has exceeded the allowed number of transactions
     * within a 60-second rolling window (velocity check).
     *
     * @param accountNumber the account to check
     * @return true if the velocity limit has been exceeded
     */
    private boolean isVelocityExceeded(String accountNumber) {
        String key = "fraud:velocity" + accountNumber;
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, 60, TimeUnit.SECONDS);
        }

        log.info("velocity check - account : {} count: {}/{}",
                accountNumber, count, maxTransactionPerMinute);

        return count != null && count > maxTransactionPerMinute;
    }

    /**
     * Checks if the transaction amount is suspiciously high.
     * Flags transactions that exceed a fixed threshold (default: 50,000).
     *
     * @param amount the transaction amount
     * @return true if the amount is suspicious
     */
    private boolean isAmountSuspicious(String accountNumber,  BigDecimal amount) {
        String avgKey = "fraud:avg_amount:" + accountNumber;
        String avgStr = redisTemplate.opsForValue().get(avgKey);

        if(avgStr == null) {
            redisTemplate.opsForValue().set(avgKey, amount.toString()); 
            return false;
        }

        BigDecimal newAvg = new BigDecimal(avgStr);
        BigDecimal threshold = avgAmount.multiply(BigDecimal.valueOf(suspiciousAmountMultiplier));

        BigDecimal newAvg = avgAmount.add(amount).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)

        redisTemplate.opsForValue().set(avgKey, newAvg.toString(), 60);

        log.info("amount check - amount : {}  threshold: {} supicious: {}", 
                amount, threshold, amount.compareTo(threshold));   

        if (amount.compareTo(threshold) > 0) {
            redisTemplate.opsForValue().set(avgKey, amount.toString());
            return true;
        }
        return amount.compareTo(threshold) > 0;
    }

    /**
     * Checks if the transaction would drain more than 90% of the sender's balance,
     * which is a strong indicator of account takeover or fraud.
     *
     * @param senderBalance the current balance of the sender
     * @param amount        the transaction amount
     * @return true if the transaction drains more than 90% of the balance
     */
    private boolean isBalanceCheckFailed(BigDecimal senderBalance, BigDecimal amount) {
        BigDecimal maxAllowed = senderBalance.multiply(
                BigDecimal.valueOf(maxBalancePercentage));

        log.info("balance check - maxAllowed : {} suscpecious : {}", amount, maxAllowed,
                amount.compareTo(maxAllowed) > 0);

        return amount.compareTo(maxAllowed) > 0;
    }
}
