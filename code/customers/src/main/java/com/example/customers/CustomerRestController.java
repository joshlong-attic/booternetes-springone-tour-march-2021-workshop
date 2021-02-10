package com.example.customers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
class CustomerRestController {

    private final CustomerRepository customerRepository;

    @GetMapping("/customers")
    Flux<Customer> get() {
        return this.customerRepository.findAll();
    }
}
