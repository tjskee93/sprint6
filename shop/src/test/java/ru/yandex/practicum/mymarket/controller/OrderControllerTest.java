package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.dto.OrderDTO;
import ru.yandex.practicum.mymarket.service.OrderService;
import ru.yandex.practicum.mymarket.service.UserService;

import java.util.List;

import static org.mockito.Mockito.when;

@WebFluxTest(OrderController.class)
@WithMockUser(username = "testuser", roles = "USER")
class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private UserService userService;
    @Test
    void getOrders_ShouldReturnOrdersPage() throws Exception {
        List<OrderDTO> orders = List.of(
                new OrderDTO(1L, List.of(), 5000L)
        );
        when(orderService.getAllOrders(1L)).thenReturn(Flux.fromIterable(orders));
        when(userService.getCurrentUserId()).thenReturn(Mono.just(1L));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    org.assertj.core.api.Assertions.assertThat(body).contains("orders");
                });
    }

    @Test
    void getOrder_WithNewOrderFlag_ShouldReturnOrderPage() throws Exception {
        OrderDTO order = new OrderDTO(1L, List.of(), 5000L);
        when(orderService.getOrderById(1L, 1L)).thenReturn(Mono.just(order));
        when(userService.getCurrentUserId()).thenReturn(Mono.just(1L));

        webTestClient.get()
                .uri("/orders/1?newOrder=true")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    org.assertj.core.api.Assertions.assertThat(body).contains("order");
                });
    }

    @Test
    void buy_ShouldCreateOrderAndRedirect() throws Exception {
        Order order = new Order();
        order.setId(1L);
        when(orderService.createOrder(1L)).thenReturn(Mono.just(order));
        when(userService.getCurrentUserId()).thenReturn(Mono.just(1L));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())//добавляем токен
                .post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/1?newOrder=true");
    }
}