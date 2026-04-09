package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
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
        return itemService.getItemById(id)
                .doOnNext(item -> model.addAttribute("item", item))
                .thenReturn("item");
    }



    @PostMapping("/items/{id}")
    public Mono<String> updateItemCart(@PathVariable Long id, @RequestParam String action, Model model) {
        System.out.println("post.id = " + id);
        return itemService.updateCart(id, action)
                .then(itemService.getItemById(id))
                .doOnNext(item -> model.addAttribute("item", item))
                .thenReturn("item");
    }
}