package com.banking.transectionservice.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

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
    private final TransactionMapper transactionMapper;

    public TransactionResponse transfer(TransferRequest request) {
        log.info("Processing transfer from {} to {}", request.getSenderAccountNumber(), request.getRecieverAccountNumber());
        
        Transaction transaction = new Transaction();
        transaction.setSenderAccountNumber(request.getSenderAccountNumber());
        transaction.setRecieverAccountNumber(request.getRecieverAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setReferenceNumber(UUID.randomUUID().toString());
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        return transactionMapper.mapToResponse(savedTransaction);
    }

    public TransactionResponse getTransaction(String transactionId) {
        log.info("Fetching transaction with ID: {}", transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));
                
        return transactionMapper.mapToResponse(transaction);
    }
}
