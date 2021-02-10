package com.example.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@RequiredArgsConstructor
class CrmClient {

    private final WebClient webClient;
    private final RSocketRequester rSocketRequester;

    Flux<CustomerOrders> getCustomerOrders() {
        return getCustomers()
                .flatMap(customer ->
                        Mono.zip(Mono.just(customer), getOrdersFor(customer.getId()).collectList())
                )
                .map(tuple -> new CustomerOrders(tuple.getT1(), tuple.getT2()));
    }

    Flux<Customer> getCustomers() {
        return this.webClient
                .get()
                .uri("http://localhost:8080/customers")
                .retrieve()
                .bodyToFlux(Customer.class)
                .onErrorResume(ex -> Flux.empty())
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(10, Duration.ofSeconds(1)));
    }

    Flux<Order> getOrdersFor(Integer customerId) {
        return this.rSocketRequester
                .route("orders.{customerId}", customerId)
                .retrieveFlux(Order.class);
    }
}
