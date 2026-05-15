package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.dto.CartUpdateDTO;
import ru.yandex.practicum.mymarket.service.ItemService;
import ru.yandex.practicum.mymarket.service.UserService;
import ru.yandex.practicum.mymarket.utils.Utils;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private static final Logger log = LoggerFactory.getLogger(ItemController.class);

    @Autowired
    private ItemService itemService;
    @Autowired
    private final UserService userService;

    @GetMapping({"/", "/items"})
    public Mono<String> getItems(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NO") String sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            Model model
    ) {

        return userService.getCurrentUserId()
                .flatMap(userId -> Mono.zip(
                itemService.getItems(search, sort, pageNumber, pageSize, userId),
                itemService.getPagingInfo(search, sort, pageNumber, pageSize)
        ).doOnNext(tuple -> {
            model.addAttribute("items", tuple.getT1());
            model.addAttribute("paging", tuple.getT2());
            model.addAttribute("search", search);
            model.addAttribute("sort", sort);
            model.addAttribute("isAuthenticated", userId != 0L);
            model.addAttribute("username", Utils.getCurrentUserName());
        })).thenReturn("items");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/items")
    public Mono<String> updateCart(@ModelAttribute CartUpdateDTO request) {

        return userService.getCurrentUserId()
                .flatMap(userId -> {
                    // Проверяем, авторизован ли пользователь (userId == 0 означает анонимного)
                    if (userId == 0L) {
                        log.debug("Неавторизованный пользователь пытается обновить корзину, перенаправление на логин");
                        return Mono.just("redirect:/login");
                    }

                    // Авторизованный пользователь - обновляем корзину
                    return itemService.updateCart(userId, request.getId(), request.getAction())
                            .thenReturn("redirect:/items?search=" +
                                    (request.getSearch() != null ? request.getSearch() : "") +
                                    "&sort=" + request.getSort() +
                                    "&pageNumber=" + request.getPageNumber() +
                                    "&pageSize=" + request.getPageSize());
                })
                .onErrorResume(e -> {
                    log.error("Ошибка при обновлении корзины: {}", e.getMessage());
                    return Mono.just("redirect:/items?error=true");
                });
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable Long id, Model model) {
        log.debug("GET /items/{}", id);

        return userService.getCurrentUserId()
                .flatMap(userId -> itemService.getItemWithCartCount(userId, id)
                .doOnNext(item -> {
                    model.addAttribute("item", item);
                    model.addAttribute("isAuthenticated", userId != 0L);
                    model.addAttribute("username", Utils.getCurrentUserName());
                }))
                .doOnError(e -> log.error("Ошибка загрузки товара {}: {}", id, e.getMessage()))
                .thenReturn("item");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/items/{id}")
    public Mono<String> updateItemCart(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "PLUS") String action,
            Model model
    ) {

        return userService.getCurrentUserId()
                .flatMap(userId -> {
                    // Проверяем, авторизован ли пользователь (userId == 0 означает анонимного)
                    if (userId == 0L) {
                        log.debug("Неавторизованный пользователь пытается добавить товар {}, перенаправление на логин", id);
                        return Mono.just("redirect:/login");
                    }
                    System.out.println(action);

                    // Авторизованный пользователь - обновляем корзину и возвращаемся на страницу товара
                    return itemService.updateCart(userId, id, action)
                            .then(itemService.getItemWithCartCount(userId, id))
                            .doOnNext(item -> {model.addAttribute("item", item);
                                model.addAttribute("isAuthenticated", true);
                                model.addAttribute("username", Utils.getCurrentUserName());})
                            .thenReturn("item");
                })
                .onErrorResume(e -> {
                    log.error("Ошибка при обновлении товара в корзине: {}", e.getMessage());
                    model.addAttribute("errorMessage", "Ошибка обновления корзины");
                    // В случае ошибки все равно показываем страницу товара
                    return itemService.getItemWithCartCount(0L, id)
                            .doOnNext(item -> {model.addAttribute("item", item);
                                model.addAttribute("isAuthenticated", true);
                                model.addAttribute("username", Utils.getCurrentUserName());})
                            .thenReturn("item");
                });
    }
}