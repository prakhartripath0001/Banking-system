package com.banking.transectionservice.client;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "account-service", url = "${account.service.url}")
public interface AccountServiceClient {
    @PutMapping("/api/v1/accounts/{accountNumber}/deduct")
    public String deductBalance(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount);

    // SAGA step 1: deduct from sender
    // SAGA step 4 : refund to sender (compensating transaction)

    @PutMapping("/api/v1/accounts/{accountNumber}/refund")
    public String refundBalance(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount);
}
