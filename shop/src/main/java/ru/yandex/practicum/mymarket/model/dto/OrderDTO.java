package ru.yandex.practicum.mymarket.model.dto;

import java.util.List;

public record OrderDTO(
        Long id,
        List<ItemDTO> items,
        Long totalSum
) {}
