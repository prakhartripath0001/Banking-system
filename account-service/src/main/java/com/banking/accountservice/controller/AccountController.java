package com.banking.accountservice.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banking.accountservice.dto.AccountReponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.mapper.AccountMapper;
import com.banking.accountservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@AllArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @PostMapping
    public ResponseEntity<AccountReponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        log.info("Received request to create account for email: {}", request.getEmail());
        Account account = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountMapper.mapToResponse(account));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountReponse> getAccount(@PathVariable String accountNumber) {
        Account account = accountService.getAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(accountMapper.mapToResponse(account));
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getBalance(accountNumber));
    }

    @PostMapping("/{accountNumber}/block")
    public ResponseEntity<Void> blockAccount(@PathVariable String accountNumber) {
        log.info("Received request to block account with account number: {}", accountNumber);
        accountService.blockAccount(accountNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{accountNumber}/deduct")
    public ResponseEntity<Void> deductBalance(@PathVariable String accountNumber, @RequestParam BigDecimal amount) {
        log.info("Received request to deduct {} from account with account number: {}", amount, accountNumber);
        accountService.deductBalance(accountNumber, amount);
        return ResponseEntity.ok().build();
    }
}
