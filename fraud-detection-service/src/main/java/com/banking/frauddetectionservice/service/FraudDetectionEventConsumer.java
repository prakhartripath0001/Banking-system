package com.banking.frauddetectionservice.service;

import java.util.Map;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FraudDetectionEventConsumer {
    public void consumeTransectionInitiated(@Payload Map<String, Object> payload) {
        log.info("Received transaction for fraud check: {}", payload.get("transactionId"));

        try {

        } catch (Exception e) {

        }
    }
}
