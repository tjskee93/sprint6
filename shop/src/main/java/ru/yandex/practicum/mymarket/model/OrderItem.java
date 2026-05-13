package ru.yandex.practicum.mymarket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("order_items")
public class OrderItem {

    @Id
    private Long id;

    private Long orderId;

    private Long itemId;

    private String title;

    private Long price;

    private int quantity;

    @Transient
    private Item item;

    public OrderItem(Long orderId, Long itemId, String title, Long price, int quantity) {
        this.orderId = orderId;
        this.itemId = itemId;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
    }
}