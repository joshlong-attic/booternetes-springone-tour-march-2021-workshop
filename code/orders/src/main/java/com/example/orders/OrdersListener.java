package com.example.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.stream.IntStream;

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
