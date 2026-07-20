package com.banking.frauddetectionservice.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudDetectionService {
    public void checkTransaction(Map<String, String> payload) {
        String transactionId = (String) payload.get("transactionId");
        String accountNumber = (String) payload.get("senderAccountNumber");
        String amount = (String) payload.get("amount");

    }
}
