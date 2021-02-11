package com.example.gateway;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Log4j2
@Configuration
class ApiGatewayConfiguration {

	@Bean
	RouteLocator gateway(
		@Value("${gateway.customers.hostname-and-port}") String customersHostAndPort,
		RouteLocatorBuilder rlb
	) {
		log.info("sending request to " + customersHostAndPort + '.');
		return rlb
			.routes()
			.route(routeSpec -> routeSpec
				.path("/c")
				.filters(gatewayFilterSpec -> gatewayFilterSpec
					.addResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
					.rewritePath("\\/c", "/customers")
				)
				.uri(customersHostAndPort)
			)
			.build();
	}

}
