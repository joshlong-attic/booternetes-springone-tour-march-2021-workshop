package com.example.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
class OrderRSocketController {

	private final OrderRepository orderRepository;

	@MessageMapping("orders.{customerId}")
	Flux<Order> getByCustomerId(@DestinationVariable Integer customerId) {
		return this.orderRepository.findByCustomerId(customerId);
	}
}
