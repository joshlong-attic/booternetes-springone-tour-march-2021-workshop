package com.example.orders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@Table("orders")
@NoArgsConstructor
class Order {

    @Id
    private Integer id;
    private Integer customerId;
}
