package com.example.customers;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
class CustomersListener {

    private final CustomerRepository customerRepository;
    private final DatabaseClient dbc;

    @EventListener(ApplicationReadyEvent.class)
    public void ready() {
        var ddl = dbc.sql(
                "create table customer( id serial primary key, name varchar(255) not null )")
                .fetch()
                .rowsUpdated();
        var names = Flux
                .just("Cora", "Coté", "Jakub", "Josh", "Mario", "Mark", "Nate", "Paul", "Tasha", "Tiffany")
                .map(name -> new Customer(null, name))
                .flatMap(customerRepository::save);

        ddl.thenMany(names).thenMany(customerRepository.findAll()).subscribe(System.out::println);

    }
}
