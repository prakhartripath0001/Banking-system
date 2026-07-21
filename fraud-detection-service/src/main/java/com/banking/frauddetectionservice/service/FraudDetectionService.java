package com.banking.frauddetectionservice.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.banking.frauddetectionservice.client.AccountServiceClient;
import com.banking.frauddetectionservice.model.FraudCheckResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudDetectionService {

    private final AccountServiceClient accountServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String VERIFICATION_REQUIRED_TOPIC = "verification.required";
    private static final String FRAUD_CHECK_CLEAN_RESULT = "fraud.check.clean.result";

    // Velocity check: tracks timestamps of recent transactions per account
    private static final int VELOCITY_LIMIT = 5;
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
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(VELOCITY_WINDOW_SECONDS);

        // Get or create the list of recent transaction timestamps for this account
        List<Instant> timestamps = transactionTimestamps
                .computeIfAbsent(accountNumber, k -> new ArrayList<>());

        // Remove timestamps that are outside the rolling window
        timestamps.removeIf(t -> t.isBefore(windowStart));

        // Record the current transaction timestamp
        timestamps.add(now);

        boolean exceeded = timestamps.size() > VELOCITY_LIMIT;
        if (exceeded) {
            log.warn("Velocity check FAILED for account {}: {} transactions in last {} seconds",
                    accountNumber, timestamps.size(), VELOCITY_WINDOW_SECONDS);
        }
        return exceeded;
    }

    /**
     * Checks if the transaction amount is suspiciously high.
     * Flags transactions that exceed a fixed threshold (default: 50,000).
     *
     * @param amount the transaction amount
     * @return true if the amount is suspicious
     */
    private boolean isAmountSuspicious(BigDecimal amount) {
        boolean suspicious = amount.compareTo(SUSPICIOUS_AMOUNT_THRESHOLD) > 0;
        if (suspicious) {
            log.warn("Amount check FAILED: transaction amount {} exceeds threshold {}",
                    amount, SUSPICIOUS_AMOUNT_THRESHOLD);
        }
        return suspicious;
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
        if (senderBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        BigDecimal drainRatio = amount.divide(senderBalance, 4, java.math.RoundingMode.HALF_UP);
        boolean failed = drainRatio.compareTo(BALANCE_DRAIN_THRESHOLD) > 0;
        if (failed) {
            log.warn("Balance check FAILED: transaction amount {} is {}% of balance {}",
                    amount, drainRatio.multiply(BigDecimal.valueOf(100)).setScale(2, java.math.RoundingMode.HALF_UP), senderBalance);
        }
        return failed;
    }
}
