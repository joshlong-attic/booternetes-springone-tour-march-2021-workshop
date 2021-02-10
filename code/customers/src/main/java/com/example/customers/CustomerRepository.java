package com.example.customers;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

interface CustomerRepository extends ReactiveCrudRepository<Customer, Integer> {
}
