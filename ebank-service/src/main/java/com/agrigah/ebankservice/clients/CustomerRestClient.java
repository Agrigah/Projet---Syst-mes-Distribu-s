package com.agrigah.ebankservice.clients;

import com.agrigah.ebankservice.model.Customer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Client OpenFeign : appelle customer-service en passant par Eureka
 * (le nom CUSTOMER-SERVICE est résolu par le Discovery Service + LoadBalancer).
 */
@FeignClient(name = "CUSTOMER-SERVICE")
public interface CustomerRestClient {

    @GetMapping("/customers/{id}")
    Customer findCustomerById(@PathVariable("id") Long id);

    @GetMapping("/customers")
    List<Customer> allCustomers();
}
