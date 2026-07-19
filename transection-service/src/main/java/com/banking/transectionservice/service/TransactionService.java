package com.banking.transectionservice.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.banking.transectionservice.client.AccountServiceClient;
import com.banking.transectionservice.dto.TransactionResponse;
import com.banking.transectionservice.dto.TransferRequest;
import com.banking.transectionservice.entity.Transaction;
import com.banking.transectionservice.entity.enums.TransactionStatus;
import com.banking.transectionservice.entity.enums.TransactionType;
import com.banking.transectionservice.mapper.TransactionMapper;
import com.banking.transectionservice.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final TransactionMapper transactionMapper;

    private static final String TRANSACTION_INITIATED_TOPIC = "transaction:initiated";
    private static final String TRANSACTION_COMPLETED_TOPIC = "transaction:completed";
    private static final String TRANSACTION_REFUNDED_TOPIC = "transaction:refunded";

    /**
     * Saga Step 1: Initiate Transfer
     * 
     * This method starts the distributed transaction saga by performing the
     * following:
     * 1. Deducts the requested amount from the sender's account via a Feign client.
     * 2. Saves the initial transaction record to the database with a PROCESSING
     * state.
     * 3. Publishes a "transaction initiated" event to Kafka to trigger a fraud
     * check.
     * 
     * @param request The transfer details (sender, receiver, amount, etc.)
     * @return TransactionResponse containing the newly created transaction details
     */
    public TransactionResponse transfer(TransferRequest request) {
        log.info("Processing transfer from {} to {}", request.getSenderAccountNumber(),
                request.getRecieverAccountNumber(),
                request.getAmount());

        // 1. Deduct the requested amount from the sender's account via a Feign client.
        accountServiceClient.deductBalance(request.getSenderAccountNumber(), request.getAmount());

        Transaction transaction = new Transaction();
        transaction.setSenderAccountNumber(request.getSenderAccountNumber());
        transaction.setRecieverAccountNumber(request.getRecieverAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());

        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.PROCESSING);
        transaction.setReferenceNumber(UUID.randomUUID().toString());

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction saved as PROCESSING", savedTransaction.getId());

        return transactionMapper.mapToResponse(savedTransaction);
    }

    public TransactionResponse getTransaction(String transactionId) {
        log.info("Fetching transaction with ID: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));

        return transactionMapper.mapToResponse(transaction);
    }

    public java.util.List<TransactionResponse> getTransactionHistory(String accountNumber) {
        log.info("Fetching transaction history for account: {}", accountNumber);

        return transactionRepository.findBySenderAccountNumberOrRecieverAccountNumber(accountNumber, accountNumber)
                .stream()
                .map(transactionMapper::mapToResponse)
                .toList();
    }

    public TransactionResponse verifyOTP(String transactionId, String otp) {
        log.info("Verifying OTP for transaction ID: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));

        if ("123456".equals(otp)) {
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);
            log.info("OTP verified successfully for transaction ID: {}", transactionId);
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Invalid OTP");
            transactionRepository.save(transaction);
            throw new RuntimeException("Invalid OTP for transaction ID: " + transactionId);
        }

        return transactionMapper.mapToResponse(transaction);
    }
}
