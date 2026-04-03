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
@Table("items")
public class Item {

    @Id
    private Long id;

    private String title;

    private String description;

    private String imgPath;

    private Long price;

    @Transient  // <- правильная аннотация для Spring Data R2DBC
    private int count;

    public Item(String title, String description, String imgPath, Long price) {
        this.title = title;
        this.description = description;
        this.imgPath = imgPath;
        this.price = price;
        this.count = 0;
    }

}