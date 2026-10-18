package com.agrigah.ebankservice.service;

import com.agrigah.ebankservice.clients.CustomerRestClient;
import com.agrigah.ebankservice.entities.BankAccount;
import com.agrigah.ebankservice.model.Customer;
import com.agrigah.ebankservice.repository.BankAccountRepository;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EbankService {
    private final BankAccountRepository accountRepository;
    private final CustomerRestClient customerRestClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public EbankService(BankAccountRepository accountRepository,
                        CustomerRestClient customerRestClient,
                        CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.accountRepository = accountRepository;
        this.customerRestClient = customerRestClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public List<BankAccount> getAllBankAccounts() {
        List<BankAccount> accounts = accountRepository.findAll();
        enrichWithCustomers(accounts);
        return accounts;
    }

    public List<BankAccount> getBankAccountsByCustomerId(Long customerId) {
        List<BankAccount> accounts = accountRepository.findByCustomerId(customerId);
        enrichWithCustomers(accounts);
        return accounts;
    }

    public BankAccount getBankAccountById(String id) {
        BankAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bank account not found"));
        account.setCustomer(fetchCustomer(account.getCustomerId()));
        return account;
    }

    public BankAccount saveBankAccount(BankAccount bankAccount) {
        bankAccount.setCreatedAt(new Date());
        return accountRepository.save(bankAccount);
    }

    /** Un seul appel à customer-service par client distinct (évite le N+1). */
    private void enrichWithCustomers(List<BankAccount> accounts) {
        Map<Long, Customer> cache = new HashMap<>();
        for (BankAccount account : accounts) {
            account.setCustomer(cache.computeIfAbsent(account.getCustomerId(), this::fetchCustomer));
        }
    }

    /**
     * Appel Feign protégé par un Circuit Breaker :
     * si customer-service est en panne / trop lent / circuit ouvert -> fallback.
     */
    private Customer fetchCustomer(Long customerId) {
        return circuitBreakerFactory.create("customerService").run(
                () -> customerRestClient.findCustomerById(customerId),
                throwable -> defaultCustomer(customerId)
        );
    }

    private Customer defaultCustomer(Long customerId) {
        return Customer.builder()
                .id(customerId)
                .name("Service client indisponible")
                .email("indisponible")
                .build();
    }
}
