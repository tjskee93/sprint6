package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.dto.ItemDTO;
import ru.yandex.practicum.mymarket.model.dto.PagingDTO;
import ru.yandex.practicum.mymarket.service.ItemService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ItemService itemService;

    @Test
    void getItems_ShouldReturnItemsPage() throws Exception {
        List<List<ItemDTO>> items = List.of(
                List.of(new ItemDTO(1L, "Тестовый товар", "Описание", "images/test.jpg", 1000L, 0))
        );

        PagingDTO paging = new PagingDTO(5, 1, false, true, 10L, 50L);

        when(itemService.getItems(any(), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(items));
        when(itemService.getPagingInfo(any(), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(paging));

        webTestClient.get()
                .uri("/items?pageNumber=1&pageSize=5")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    org.assertj.core.api.Assertions.assertThat(body).contains("items");
                });
    }

    @Test
    void updateCart_ShouldRedirect() throws Exception {
        when(itemService.updateCart(any(), any())).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("id=1&action=PLUS&pageNumber=1&pageSize=5")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items?search=&sort=NO&pageNumber=1&pageSize=5");
    }

    @Test
    void updateCart_WithSearch_ShouldRedirectWithSearch() throws Exception {
        when(itemService.updateCart(any(), any())).thenReturn(Mono.empty());
        String encodedSearch = URLEncoder.encode("тест", StandardCharsets.UTF_8);
        String expectedLocation = "/items?search=" + encodedSearch + "&sort=ALPHA&pageNumber=2&pageSize=10";

        webTestClient.post()
                .uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("id=1&search=тест&action=PLUS&sort=ALPHA&pageNumber=2&pageSize=10")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().exists("Location");
    }

}