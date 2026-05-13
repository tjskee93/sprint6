package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.dto.CartUpdateDTO;
import ru.yandex.practicum.mymarket.service.ItemService;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private static final Logger log = LoggerFactory.getLogger(ItemController.class);

    @Autowired
    private ItemService itemService;

    @GetMapping({"/", "/items"})
    public Mono<String> getItems(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NO") String sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            Model model
    ) {

        return Mono.zip(
                itemService.getItems(search, sort, pageNumber, pageSize),
                itemService.getPagingInfo(search, sort, pageNumber, pageSize)
        ).doOnNext(tuple -> {
            model.addAttribute("items", tuple.getT1());
            model.addAttribute("paging", tuple.getT2());
            model.addAttribute("search", search);
            model.addAttribute("sort", sort);
        }).thenReturn("items");
    }

    @PostMapping("/items")
    public Mono<String> updateCart(@ModelAttribute CartUpdateDTO request) {

        return itemService.updateCart(request.getId(), request.getAction())
                .thenReturn("redirect:/items?search=" +
                        (request.getSearch() != null ? request.getSearch() : "") +
                        "&sort=" + request.getSort() +
                        "&pageNumber=" + request.getPageNumber() +
                        "&pageSize=" + request.getPageSize());
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable Long id, Model model) {
        log.debug("GET /items/{}", id);

        return itemService.getItemWithCartCount(id)
                .doOnNext(item -> {
                    model.addAttribute("item", item);
                })
                .doOnError(e -> log.error("Ошибка загрузки товара {}: {}", id, e.getMessage()))
                .thenReturn("item");
    }

    @PostMapping("/items/{id}")
    public Mono<String> updateItemCart(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "PLUS") String action,
            Model model
    ) {

        return itemService.updateCart(id, action)
                .then(itemService.getItemWithCartCount(id))
                .doOnNext(item -> {
                    model.addAttribute("item", item);
                })
                .thenReturn("item");
    }
}