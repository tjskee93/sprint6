package ru.yandex.practicum.mymarket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("orders")
public class Order {

    @Id
    private Long id;

    private LocalDateTime orderDate;

    @Transient
    private List<OrderItem> items = new ArrayList<>();

    @Transient
    private Long totalSum;

    public Order(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public Long getTotalSum() {
        if (totalSum == null && items != null) {
            totalSum = items.stream()
                    .mapToLong(item -> item.getPrice() * item.getQuantity())
                    .sum();
        }
        return totalSum;
    }
}