package com.banking.frauddetectionservice.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.banking.frauddetectionservice.client.AccountServiceClient;
import com.banking.frauddetectionservice.model.FraudCheckResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudDetectionService {

    private final AccountServiceClient accountServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String VERIFICATION_REQUIRED_TOPIC = "verification.required";
    private static final String FRAUD_CHECK_CLEAN_RESULT = "fraud.check.clean.result";

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

        FraudCheckResult result = performFraudChecks(accountNumber, amount, senderBalance);

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
            return new FraudCheckResult(true, "too many transaction in 60 seconds" + "velcoity limit exceeded");
        }

        if (isAmountSuspicious(accountNumber, amount)) {
            return new FraudCheckResult(true, "Amount more than 50000, OTP is required");
        }

        return new FraudCheckResult(false, null);
    }

}
