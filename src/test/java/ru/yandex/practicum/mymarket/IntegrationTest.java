package ru.yandex.practicum.mymarket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import static org.hamcrest.Matchers.*;


@SpringBootTest
@AutoConfigureWebTestClient
class IntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemRepository itemRepository;

    private Long testItemId;

    @BeforeEach
    void setUp() {
        // Очищаем БД
        itemRepository.deleteAll().block();

        // Создаем тестовые товары
        Item item1 = new Item(null, "Интеграционный тест", "Описание", "images/test.jpg", 1000L, 0);
        Item item2 = new Item(null, "Ааа товар", "Описание", "images/test1.jpg", 500L, 0);
        Item item3 = new Item(null, "Яяя товар", "Описание", "images/test2.jpg", 2000L, 0);

        // Сохраняем товары и получаем ID первого
        Flux.just(item1, item2, item3)
                .flatMap(itemRepository::save)
                .collectList()
                .block();

        // Получаем ID тестового товара
        testItemId = itemRepository.findAll()
                .filter(item -> item.getTitle().equals("Интеграционный тест"))
                .next()
                .map(Item::getId)
                .block();
    }

    @Test
    void fullWorkflow_ShouldWorkCorrectly() {
        // 1. Проверка главной страницы
        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk();

        // 2. Добавление товара в корзину
        webTestClient.post()
                .uri("/items")
                .bodyValue("id=" + testItemId + "&action=PLUS&pageNumber=1&pageSize=5")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .exchange()
                .expectStatus().is3xxRedirection();

        // 3. Проверка корзины (должен быть добавленный товар)
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Интеграционный тест"));

        // 4. Оформление покупки
        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", "/orders/\\d+\\?newOrder=true");

        // 5. Проверка страницы заказов
        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Интеграционный тест"));

        // 6. Проверка очистки корзины
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(not(containsString("Интеграционный тест")));
    }

    @Test
    void search_ShouldWorkCorrectly() {
        webTestClient.get()
                .uri("/items?search=тест")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Интеграционный тест"))
                .value(not(containsString("Ааа товар")));
    }

    @Test
    void searchAndSort_ShouldWorkCorrectly() {
        // Тест поиска
        webTestClient.get()
                .uri("/items?search=тест")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Интеграционный тест"))
                .value(not(containsString("Ааа товар")));

        // Тест сортировки по алфавиту
        webTestClient.get()
                .uri("/items?sort=ALPHA")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("Ааа товар"))
                .value(containsString("Интеграционный тест"))
                .value(containsString("Яяя товар"));

        // Тест сортировки по цене
        webTestClient.get()
                .uri("/items?sort=PRICE")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(containsString("500 руб"))
                .value(containsString("1000 руб"))
                .value(containsString("2000 руб"));
    }
}