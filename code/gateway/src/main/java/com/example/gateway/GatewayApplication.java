package com.example.gateway;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collection;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

    @Bean
    RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {
        return builder.tcp("localhost", 8181);
    }

    @Bean
    RouteLocator gateway(RouteLocatorBuilder rlb) {
        return rlb
                .routes()
                .route(routeSpec -> routeSpec
                        .path("/c")
                        .filters(gatewayFilterSpec -> gatewayFilterSpec
                                .addResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                                .rewritePath("\\/c", "/customers")
                        )
                        .uri("http://localhost:8080/")
                )
                .build();
    }

}

@RestController
@RequiredArgsConstructor
class CustomerOrdersRestController {

    private final CrmClient crmClient;

    @GetMapping("/cos")
    Flux<CustomerOrders> get() {
        return this.crmClient.getCustomerOrders();
    }
}

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

@Data
@
        AllArgsConstructor
@NoArgsConstructor
class CustomerOrders {

    private Customer customer;
    private Collection<Order> orders;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Order {
    private Integer id;
    private Integer customerId;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Customer {
    private Integer id;
    private String name;
}