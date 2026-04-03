package ru.yandex.practicum.mymarket.model.dto;

import ru.yandex.practicum.mymarket.model.Item;

public record ItemDTO(
        Long id,
        String title,
        String description,
        String imgPath,
        Long price,
        int count
) {
    public static ItemDTO fromEntity(Item item) {
        return new ItemDTO(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImgPath(),
                item.getPrice(),
                item.getCount()
        );
    }
}
