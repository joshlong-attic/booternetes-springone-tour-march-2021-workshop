package com.example.gateway;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
class CustomerOrders {

    private Customer customer;
    private Collection<Order> orders;
}
