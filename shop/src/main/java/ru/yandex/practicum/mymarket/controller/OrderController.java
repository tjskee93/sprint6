package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.service.OrderService;
import ru.yandex.practicum.mymarket.service.UserService;
import ru.yandex.practicum.mymarket.utils.Utils;

@Controller
@RequiredArgsConstructor
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;

    @GetMapping("/orders")
    public Mono<String> getOrders(Model model) {
        return userService.getCurrentUserId()
                .flatMap( userId ->
                        orderService.getAllOrders(userId)
                    .collectList()
                    .doOnNext(orders -> {
                        model.addAttribute("orders", orders);
                        model.addAttribute("username", Utils.getCurrentUserName());})
                ).thenReturn("orders");
    }

    @GetMapping("/orders/{id}")
    public Mono<String> getOrder(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean newOrder,
            Model model
    ) {
        return userService.getCurrentUserId()
                .flatMap( userId ->
                        orderService.getOrderById(userId, id))
                .doOnNext(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("newOrder", newOrder);
                    model.addAttribute("username", Utils.getCurrentUserName());
                })
                .thenReturn("order");
    }

    @PostMapping("/buy")
    public Mono<String> buy() {
        return userService.getCurrentUserId()
                        .flatMap(userId -> orderService.createOrder(userId))
                .map(order -> "redirect:/orders/" + order.getId() + "?newOrder=true")
                .onErrorResume(error -> {return Mono.just("redirect:/cart/items");});
    }
}