package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.dto.CartUpdateDTO;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.PaymentClientService;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    @Autowired
    private CartService cartService;
    @Autowired
    private PaymentClientService paymentClientService;

    @GetMapping("/items")
    public Mono<String> getCart(Model model) {
        return Mono.zip(
                cartService.getCartItems().collectList(),
                cartService.getCartTotal(),
                paymentClientService.getBalance()
        ).doOnNext(tuple -> {
            model.addAttribute("items", tuple.getT1());
            model.addAttribute("total", tuple.getT2());
            long balance = tuple.getT3();
            model.addAttribute("balance", balance);
            model.addAttribute("canBuy", balance >= tuple.getT2() && balance != -1);
            model.addAttribute("paymentAvailable", balance != -1);
            if (balance == -1) {
                model.addAttribute("errorMessage", "Сервис платежей недоступен. Пожалуйста, попробуйте позже.");
            } else if (balance < tuple.getT2()) {
                model.addAttribute("errorMessage", "Недостаточно средств. Ваш баланс: " + balance + " руб.");
            }
        }).thenReturn("cart");
    }



    @PostMapping("/items")
    public Mono<String> updateCart(
            @ModelAttribute CartUpdateDTO request,
            Model model
    ) {

        if (request.getId() == null || request.getAction() == null) {
            return Mono.just("redirect:/cart/items");
        }

        Long id = request.getId();
        String action = request.getAction();

        Mono<Void> actionMono = switch (action) {
            case "PLUS" -> cartService.addToCart(id);
            case "MINUS" -> cartService.removeFromCart(id);
            case "DELETE" -> cartService.deleteFromCart(id);
            default -> Mono.empty();
        };

        return actionMono
                .then(Mono.zip(
                        cartService.getCartItems().collectList(),
                        cartService.getCartTotal(),
                        paymentClientService.getBalance()
                ))
                .doOnNext(tuple -> {
                    model.addAttribute("items", tuple.getT1());
                    model.addAttribute("total", tuple.getT2());
                    long balance = tuple.getT3();
                    model.addAttribute("balance", balance);
                    model.addAttribute("canBuy", balance >= tuple.getT2() && balance != -1);
                    model.addAttribute("paymentAvailable", balance != -1);
                    if (balance == -1) {
                        model.addAttribute("errorMessage", "Сервис платежей недоступен. Пожалуйста, попробуйте позже.");
                    } else if (balance < tuple.getT2()) {
                        model.addAttribute("errorMessage", "Недостаточно средств. Ваш баланс: " + balance + " руб.");
                    }
                })
                .thenReturn("cart");
    }
}
