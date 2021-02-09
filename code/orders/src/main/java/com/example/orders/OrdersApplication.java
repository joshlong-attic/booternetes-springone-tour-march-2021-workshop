package com.example.orders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.stream.IntStream;

@SpringBootApplication
public class OrdersApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdersApplication.class, args);
    }

}

@Component
@RequiredArgsConstructor
class OrdersListener {

    private final OrderRepository orderRepository;
    private final DatabaseClient dbc;

    @EventListener(ApplicationReadyEvent.class)
    public void ready() {

        var ddl = dbc
                .sql("create table orders (id serial primary  key , customer_id int not null)")
                .fetch()
                .rowsUpdated();
        var orders = Flux
                .fromStream(IntStream.range(1, 11).boxed())
                .flatMap(this::synthesizeFor)
                .flatMap(orderRepository::save);

        ddl.thenMany(orders).thenMany(orderRepository.findAll()).subscribe(System.out::println);

    }

    private Flux<Order> synthesizeFor(Integer customerId) {
        var collection = new ArrayList<Order>();
        for (var i = 0; i < (Math.random() * 10); i++)
            collection.add(new Order(null, customerId));
        return Flux.fromIterable(collection);
    }
}

@Data
@AllArgsConstructor
@Table("orders")
@NoArgsConstructor
class Order {

    @Id
    private Integer id;
    private Integer customerId;
}

interface OrderRepository extends ReactiveCrudRepository<Order, Integer> {

    Flux<Order> findByCustomerId(Integer customerId);
}

@Controller
@RequiredArgsConstructor
class OrderRSocketController {

    private final OrderRepository orderRepository;

    @MessageMapping("orders.{customerId}")
    Flux<Order> getByCustomerId(@DestinationVariable Integer customerId) {
        return this.orderRepository.findByCustomerId(customerId);
    }
}
