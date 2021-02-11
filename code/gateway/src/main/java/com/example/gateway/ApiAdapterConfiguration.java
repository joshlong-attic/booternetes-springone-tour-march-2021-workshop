package com.example.gateway;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;

@Log4j2
@Configuration
class ApiAdapterConfiguration {

	ApiAdapterConfiguration(
		@Value("${gateway.customers.hostname-and-port}") String customersHostAndPort,
		@Value("${gateway.orders.hostname-and-port}") String ordersHostAndPort
	) {
		log.info( "customers endpoint: " + customersHostAndPort);
		log.info( "orders endpoint: " + ordersHostAndPort);
	}

	@Bean
	WebClient webClient(WebClient.Builder builder) {
		return builder.build();
	}

	@Bean
	RSocketRequester rSocketRequester(@Value("${gateway.orders.hostname-and-port}") String ordersHostAndPort,
																																			RSocketRequester.Builder builder) {
		var uri = URI.create(ordersHostAndPort);
		return builder.tcp(uri.getHost(), uri.getPort());
	}
}
