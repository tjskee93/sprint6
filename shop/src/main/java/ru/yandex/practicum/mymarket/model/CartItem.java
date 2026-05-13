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
@Table("cart_items")
public class CartItem {

    @Id
    private Long id;

    private Long itemId;

    private int quantity;

    @Transient
    private Item item;

    public CartItem(Long itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }
}