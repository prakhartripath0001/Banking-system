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

    public Account getAccount(String id) {
        log.info("Fetching account with id: {}", id);
        return accountRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Account not found with id: {}", id);
                    return new RuntimeException("Account not found with id: " + id);
                });
    }

    public BigDecimal getBalance(String id) {
        return getAccount(id).getBalance();
    }

    @Transactional
    public void blockAccount(String id) {
        log.info("Blocking account with id: {}", id);
        Account account = getAccount(id);
        account.setAccountStatus(AccountStatus.BLOCKED);
        accountRepository.save(account);
    }

    @Transactional
    public void deductBalance(String id, BigDecimal amount) {
        log.info("Deducting {} from account with id: {}", amount, id);
        Account account = getAccount(id);
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            log.error("Cannot deduct balance: Account {} is not active", id);
            throw new RuntimeException("Account is not active");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            log.error("Cannot deduct balance: Insufficient balance for account {}", id);
            throw new RuntimeException("Insufficient balance");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }

    private String generateAccountNumber() {
        return String.valueOf((long) (Math.random() * 1_000_000_000_000L));
    }
}