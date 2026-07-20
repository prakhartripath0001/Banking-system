package com.banking.transectionservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.banking.transectionservice.client.AccountServiceClient;
import com.banking.transectionservice.repository.TransactionRepository;
import com.banking.transectionservice.entity.Transaction;
import com.banking.transectionservice.entity.enums.TransactionStatus;
import com.banking.transectionservice.event.TransactionIntiatedEvent; // using this event for simplicity

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;

    private static final String TRANSACTION_COMPLETED_TOPIC = "transaction:completed";
    private static final String TRANSACTION_REFUNDED_TOPIC = "transaction:refunded";

    @KafkaListener(topics = TRANSACTION_COMPLETED_TOPIC, groupId = "transaction-service-group")
    public void consumeTransactionCompletedEvent(TransactionIntiatedEvent event) {
        log.info("Received transaction:completed event for transaction ID: {}", event.getTransactionId());

        Transaction transaction = transactionRepository.findById(event.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Update status to completed
        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);

        // SAGA step 4 (Success scenario): Credit the receiver
        log.info("Crediting receiver account {} with amount {}", event.getRecieverAccountNumber(), event.getAmount());
        accountServiceClient.creditBalance(event.getRecieverAccountNumber(), event.getAmount());
    }

    @KafkaListener(topics = TRANSACTION_REFUNDED_TOPIC, groupId = "transaction-service-group")
    public void consumeTransactionRefundedEvent(TransactionIntiatedEvent event) {
        log.info("Received transaction:refunded event for transaction ID: {}", event.getTransactionId());

        Transaction transaction = transactionRepository.findById(event.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Update status to failed/refunded
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setFailureReason("Fraud check failed, transaction refunded");
        transactionRepository.save(transaction);

        // SAGA step 4 (Failure scenario): Refund the sender
        log.info("Refunding sender account {} with amount {}", event.getSenderAccountNumber(), event.getAmount());
        accountServiceClient.refundBalance(event.getSenderAccountNumber(), event.getAmount());
    }
}
