package com.banking.accountservice.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Payload;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountEventConsumer {
    /*
     * consume transection completed event from kafka
     * 
     * @param payload
     */
    public void consumeTransectionCompleted(
            @Payload Map<String, Object> payload) {
        log.info("Received transaction completed event: {}", payload);

    }
}
