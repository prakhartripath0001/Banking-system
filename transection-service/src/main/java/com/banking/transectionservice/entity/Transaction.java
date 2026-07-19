package com.banking.transectionservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transections")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;
    private String type;
    private String status;
    private BigDecimal amount;

    private String sourceAccount;
    private String destinationAccount;
    private String sourceIFSC;
    private String destinationIFSC;

    private String remarks;
    private String referenceNumber;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String createdBy; // customer or admin
    private String updatedBy;

    private String requestId;
    private String sessionToken;

    private String externalRef1;
    private String externalRef2;
    private String externalRef3;

    private String transactionMode; // UPI, NEFT, RTGS, IMPS, Net Banking, Debit Card, Credit Card, QR

    private String sourceAccountType; // Savings, Current, Salary, Fixed Deposit, etc.
    private String destinationAccountType;

    private String sourceBankName;
    private String destinationBankName;

    private String sourceBranchName;
    private String destinationBranchName;

    private String sourceBranchCode;
    private String destinationBranchCode;

    private String sourceBranchAddress;
    private String destinationBranchAddress;

    private String sourceBranchCity;
    private String destinationBranchCity;

    private String sourceBranchState;
    private String destinationBranchState;

    private String sourceBranchCountry;
    private String destinationBranchCountry;

    private String sourceBranchPinCode;
    private String destinationBranchPinCode;

    private String sourceBranchCountryCode;
    private String destinationBranchCountryCode;

    private String sourceBranchCountryName;
    private String destinationBranchCountryName;

    private String sourceBranchCountryNameCode;
    private String destinationBranchCountryNameCode;

    private String sourceBranchCountryCodeName;
    private String destinationBranchCountryCodeName;

    private String sourceBranchCountryCodeNameCode;
    private String destinationBranchCountryCodeNameCode;

    private String sourceBranchCountryCodeNameCodeName;
    private String destinationBranchCountryCodeNameCodeName;

    private String sourceBranchCountryCodeNameCodeNameCode;
    private String destinationBranchCountryCodeNameCodeNameCode;

    private String sourceBranchCountryCodeNameCodeNameCodeName;
    private String destinationBranchCountryCodeNameCodeNameCodeName;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeName;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeName;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeName;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeName;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;

    private String sourceBranchCountryCodeNameCodeNameCodeNameCodeNameCodeNameCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCodeCode;
    private String destinationBranchCountryCodeNameCodeNameCodeNameCodeName
