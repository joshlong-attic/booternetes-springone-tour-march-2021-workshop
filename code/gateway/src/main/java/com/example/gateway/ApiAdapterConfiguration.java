package com.example.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;

@Configuration
class ApiAdapterConfiguration {

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
