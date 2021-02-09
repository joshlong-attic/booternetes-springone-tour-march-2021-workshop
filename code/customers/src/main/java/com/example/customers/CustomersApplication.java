package com.example.customers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class CustomersApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomersApplication.class, args);
    }


}


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


@RestController
@RequiredArgsConstructor
class CustomerRestController {

    private final CustomerRepository customerRepository;

    @GetMapping("/customers")
    Flux<Customer> get() {
        return this.customerRepository.findAll();
    }
}


interface CustomerRepository extends ReactiveCrudRepository<Customer, Integer> {
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Customer {

    @Id
    private Integer id;
    private String name;

}