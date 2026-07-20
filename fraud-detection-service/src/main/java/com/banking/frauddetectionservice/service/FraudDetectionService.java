package com.banking.frauddetectionservice.service;

import org.springframework.stereotype.Service;

import com.banking.frauddetectionservice.client.AccountServiceClient;
import com.banking.frauddetectionservice.model.FraudCheckResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudDetectionService {

    private final AccountServiceClient accountServiceClient;

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
        } else {
            log.info("Transaction {} passed fraud check", transactionId);
        }
    }

}
