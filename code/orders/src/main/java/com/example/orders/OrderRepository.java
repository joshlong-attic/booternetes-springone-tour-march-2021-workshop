package com.example.orders;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

interface OrderRepository extends ReactiveCrudRepository<Order, Integer> {

    Flux<Order> findByCustomerId(Integer customerId);
}
