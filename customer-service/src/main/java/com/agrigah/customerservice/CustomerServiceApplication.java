package com.agrigah.customerservice;

import lombok.Builder;
import org.apache.catalina.filters.RemoteIpFilter;
import com.agrigah.customerservice.entities.Customer;
import com.agrigah.customerservice.service.CustomerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(CustomerService customerService) {
        return args -> {
            List<String> names = List.of("Imane", "Jordan", "Michael");
            names.forEach(name->{
                customerService.saveCustomer(Customer.builder()
                        .name(name).email(name+"@gmail.com")
                        .build()
                );
            });

        };

    }

}
