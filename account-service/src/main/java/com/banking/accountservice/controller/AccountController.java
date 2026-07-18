package com.banking.accountservice.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public Account createAccount(@Valid @RequestBody CreateAccountRequest request) {
        log.info("Received request to create account for email: {}", request.getEmail());
        return accountService.createAccount(request);
    }

    @GetMapping("/{id}")
    public Account getAccount(@PathVariable String id) {
        return accountService.getAccount(id);
    }

    @GetMapping("/{id}/balance")
    public java.math.BigDecimal getBalance(@PathVariable String id) {
        return accountService.getBalance(id);
    }

    @PostMapping("/{id}/block")
    public void blockAccount(@PathVariable String id) {
        log.info("Received request to block account with id: {}", id);
        accountService.blockAccount(id);
    }

    @PostMapping("/{id}/deduct")
    public void deductBalance(@PathVariable String id, @RequestParam java.math.BigDecimal amount) {
        log.info("Received request to deduct {} from account with id: {}", amount, id);
        accountService.deductBalance(id, amount);
    }
}
