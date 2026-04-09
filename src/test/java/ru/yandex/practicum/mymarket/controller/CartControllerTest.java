package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.dto.ItemDTO;
import ru.yandex.practicum.mymarket.service.CartService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@WebFluxTest(CartController.class)
class CartControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CartService cartService;

    @Test
    void getCart_ShouldReturnCartPage() throws Exception {
        List<ItemDTO> items = List.of(
                new ItemDTO(1L, "Тестовый товар", "Описание", "images/test.jpg", 1000L, 2)
        );
        when(cartService.getCartItems()).thenReturn(Flux.fromIterable(items));
        when(cartService.getCartTotal()).thenReturn(Mono.just(2000L));

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    org.assertj.core.api.Assertions.assertThat(body).contains("cart");
                });
    }

    @Test
    void updateCart_WithPlusAction_ShouldAddToCart() throws Exception {
        List<ItemDTO> items = List.of(
                new ItemDTO(1L, "Тестовый товар", "Описание", "images/test.jpg", 1000L, 3)
        );
        when(cartService.getCartItems()).thenReturn(Flux.fromIterable(items));
        when(cartService.getCartTotal()).thenReturn(Mono.just(3000L));
        when(cartService.addToCart(anyLong())).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("id=1&action=PLUS")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    // Проверяем, что страница содержит обновленную корзину
                    org.assertj.core.api.Assertions.assertThat(body).contains("Тестовый товар");
                    org.assertj.core.api.Assertions.assertThat(body).contains("3000");
                });

        verify(cartService, times(1)).addToCart(1L);
    }
}