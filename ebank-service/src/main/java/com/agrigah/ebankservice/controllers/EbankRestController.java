package com.agrigah.ebankservice.controllers;


import com.agrigah.ebankservice.entities.BankAccount;
import com.agrigah.ebankservice.service.EbankService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EbankRestController {
    private EbankService ebankService;

    public EbankRestController(EbankService ebankService) {
        this.ebankService = ebankService;
    }
    @GetMapping("/accounts")
    public List<BankAccount> getAllBankAccounts() {
        return ebankService.getAllBankAccounts();

    }
    @GetMapping("/accounts/customer/{customerId}")
    public List<BankAccount> getBankAccountsByCustomer(@PathVariable Long customerId) {
        return ebankService.getBankAccountsByCustomerId(customerId);
    }
    @GetMapping("/accounts/{id}")
    public BankAccount getBankAccountById(@PathVariable String id) {
        return ebankService.getBankAccountById(id);
    }
    @PostMapping("/accounts")
    public BankAccount saveBankAccount(@RequestBody BankAccount bankAccount) {
        return ebankService.saveBankAccount(bankAccount);
    }
}
