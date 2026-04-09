package ru.yandex.practicum.mymarket.model.dto;

public record PagingDTO(
        int pageSize,
        int pageNumber,
        boolean hasPrevious,
        boolean hasNext,
        long totalPages,
        long totalElements
) {}
