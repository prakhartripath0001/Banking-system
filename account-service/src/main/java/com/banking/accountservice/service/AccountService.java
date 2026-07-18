package com.banking.accountservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.enums.AccountStatus;
import com.banking.accountservice.repository.AccountRepository;
import com.banking.accountservice.dto.CreateAccountRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account createAccount(CreateAccountRequest request) {
        log.info("Creating new account for email: {}", request.getEmail());
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setAccountHolderName(request.getAccountHolderName());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setAccountType(request.getAccountType());
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setBalance(request.getOpeningBalance());
        account.setDailyTransactionLimit(new BigDecimal("10000.00")); // default limit
        
        return accountRepository.save(account);
    }

    public Account getAccountByAccountNumber(String accountNumber) {
        log.info("Fetching account with account number: {}", accountNumber);
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.error("Account not found with account number: {}", accountNumber);
                    return new RuntimeException("Account not found with account number: " + accountNumber);
                });
    }

    public BigDecimal getBalance(String accountNumber) {
        return getAccountByAccountNumber(accountNumber).getBalance();
    }

    @Transactional
    public void blockAccount(String accountNumber) {
        log.info("Blocking account with account number: {}", accountNumber);
        Account account = getAccountByAccountNumber(accountNumber);
        account.setAccountStatus(AccountStatus.BLOCKED);
        accountRepository.save(account);
    }

    @Transactional
    public void deductBalance(String accountNumber, BigDecimal amount) {
        log.info("Deducting {} from account with account number: {}", amount, accountNumber);
        Account account = getAccountByAccountNumber(accountNumber);
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            log.error("Cannot deduct balance: Account {} is not active", accountNumber);
            throw new RuntimeException("Account is not active");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            log.error("Cannot deduct balance: Insufficient balance for account {}", accountNumber);
            throw new RuntimeException("Insufficient balance");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }

    private String generateAccountNumber() {
        return String.valueOf((long) (Math.random() * 1_000_000_000_000L));
    }
}