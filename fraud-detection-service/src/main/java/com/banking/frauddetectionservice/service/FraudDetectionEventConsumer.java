package com.banking.frauddetectionservice.service;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class FraudDetectionEventConsumer {

    private final FraudDetectionService fraudDetectionService;

    /*
     * Listens to transaction.intiated
     * every transaction go through fraud check before completing
     * 
     * @param payload
     */
    @KafkaListener(topics = "transection-initiated", groupId = "{fraud-detection-group}")
    public void consumeTransectionInitiated(@Payload Map<String, Object> payload) {
        log.info("Received transaction for fraud check: {}", payload.get("transactionId"));

        try {
            fraudDetectionService.checkTransaction(payload);
        } catch (Exception e) {
            log.error("Error in fraud detection for transaction {}", payload.get("transactionId"));
            log.error("Error message: {}", e.getMessage());
        }
    }
}
