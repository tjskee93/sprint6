package ru.yandex.practicum.mymarket.model.dto;

import lombok.Data;

@Data
public class CartUpdateDTO {
    private Long id;
    private String action;
    private String search;
    private String sort = "NO";
    private int pageNumber = 1;
    private int pageSize = 5;
}