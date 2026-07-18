package com.banking.accountservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.enums.AccountStatus;
import com.banking.accountservice.repository.AccountRepository;
import com.banking.accountservice.dto.AccountReponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.mapper.AccountMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Transactional
    public AccountReponse createAccount(CreateAccountRequest request) {
        log.info("Creating new account for email: {}", request.getEmail());

        if (accountRepository.existsByEmail(request.getEmail())) {
            log.error("Account creation failed: Email {} already exists", request.getEmail());
            throw new RuntimeException("Account already exists with email: " + request.getEmail());
        }

        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setAccountHolderName(request.getAccountHolderName());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setAccountType(request.getAccountType());
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setBalance(request.getOpeningBalance());
        BigDecimal dailyLimit = switch (request.getAccountType()) {
            case SAVING -> new BigDecimal("10000.00");
            case CURRENT -> new BigDecimal("100000.00");
            case FIXED_DEPOSIT -> new BigDecimal("0.00");
        };
        account.setDailyTransactionLimit(dailyLimit);

        log.info("Account created successfully with account number: {}", account.getAccountNumber());
        return accountMapper.mapToResponse(accountRepository.save(account));
    }

    public AccountReponse getAccount(String accountNumber) {
        log.info("Fetching account with account number: {}", accountNumber);
        return accountMapper.mapToResponse(getAccountByAccountNumber(accountNumber));
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

    @Transactional
    public void creditBalance(String accountNumber, BigDecimal amount) {
        log.info("Crediting {} to account with account number: {}", amount, accountNumber);
        Account account = getAccountByAccountNumber(accountNumber);
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            log.error("Cannot credit balance: Account {} is not active", accountNumber);
            throw new RuntimeException("Account is not active");
        }
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }

    private String generateAccountNumber() {
        return String.valueOf((long) (Math.random() * 1_000_000_000_000L));
    }
}