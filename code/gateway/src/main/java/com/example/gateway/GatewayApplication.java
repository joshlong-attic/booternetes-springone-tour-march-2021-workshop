package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.reactive.function.client.WebClient;

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

