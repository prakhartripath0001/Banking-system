package com.banking.transectionservice.service;

import java.util.Map;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransactionEventConsumer {
    /*
     * consume verification.required
     * Generate OTP and ask user to verify
     * 
     * @param payload
     */
    public void consumeVerificationRequired(@Payload Map<String, Object> payload) {
        try {
            String transactionId = (String) payload.get("transactionId");
            String accountNumber = (String) payload.get("senderAccountNumber");
            String reason = (String) payload.get("reason");
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
