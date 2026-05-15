package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.service.ItemCacheService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@WithMockUser(username = "testuser", roles = "USER")
class ItemCacheIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private WebTestClient webTestClientWithCsrf;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemCacheService itemCacheService;

    private Long testItemId;

    @BeforeEach
    void setUp() {
        // Создаем клиент с CSRF
        webTestClientWithCsrf = webTestClient.mutate()
                .apply(SecurityMockServerConfigurers.csrf())
                .build();
        itemRepository.deleteAll().block();
        itemCacheService.deleteItem(1L).block();

        Item item = itemRepository.save(new Item(null, "Кешируемый товар", "Описание", "images/cache.jpg", 1000L, 0)).block();
        testItemId = item.getId();
    }

    @Test
    void getItem_ShouldCacheAfterFirstRequest() {
        // Первый запрос - должен загрузить из БД и сохранить в кэш
        webTestClient.get()
                .uri("/items/" + testItemId)
                .exchange()
                .expectStatus().isOk();

        // Проверяем, что товар появился в кэше
        Boolean existsInCache = itemCacheService.hasKey(testItemId).block();
        assertThat(existsInCache).isTrue();

        // Второй запрос - должен загрузить из кэша
        webTestClient.get()
                .uri("/items/" + testItemId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void updateCart_ShouldEvictCache() {
        // Сначала загружаем товар (попадает в кэш)
        webTestClient
                .get()
                .uri("/items/" + testItemId)
                .exchange()
                .expectStatus().isOk();

        // Проверяем, что товар в кэше
        assertThat(itemCacheService.hasKey(testItemId).block()).isTrue();

        // Обновляем корзину (должно очистить кэш)
        webTestClientWithCsrf
                .post()
                .uri("/items")
                .bodyValue("id=" + testItemId + "&action=PLUS&pageNumber=1&pageSize=5")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .exchange()
                .expectStatus().is3xxRedirection();

        // Проверяем, что кэш очищен
        Boolean existsInCache = itemCacheService.hasKey(testItemId).block();
        assertThat(existsInCache).isFalse();
    }
}