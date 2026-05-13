package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import ru.yandex.practicum.mymarket.model.Item;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ItemCacheServiceIntegrationTest {


    @Autowired
    private ReactiveRedisTemplate<String, Item> reactiveRedisTemplate;

    @Autowired
    private ItemCacheService itemCacheService;

    private Item testItem;

    @BeforeEach
    void setUp() {
        testItem = new Item(1L, "Тестовый товар", "Описание", "images/test.jpg", 1000L, 0);
        reactiveRedisTemplate.keys("*")
                .flatMap(reactiveRedisTemplate::delete)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        // Очищаем Redis после каждого теста
        reactiveRedisTemplate.keys("*")
                .flatMap(reactiveRedisTemplate::delete)
                .blockLast();
    }

    @Test
    void putAndGetItem_ShouldWorkCorrectly() {
        // Сохраняем товар в кэш
        Boolean saved = itemCacheService.putItem(testItem).block();
        assertThat(saved).isTrue();

        // Получаем товар из кэша
        Item cachedItem = itemCacheService.getItem(1L).block();
        assertThat(cachedItem).isNotNull();
        assertThat(cachedItem.getId()).isEqualTo(1L);
        assertThat(cachedItem.getTitle()).isEqualTo("Тестовый товар");
    }

    @Test
    void deleteItem_ShouldRemoveFromCache() {
        // Сначала сохраняем
        itemCacheService.putItem(testItem).block();

        // Проверяем, что товар есть в кэше
        Boolean exists = itemCacheService.hasKey(1L).block();
        assertThat(exists).isTrue();

        // Удаляем товар
        Boolean deleted = itemCacheService.deleteItem(1L).block();
        assertThat(deleted).isTrue();

        // Проверяем, что товара больше нет в кэше
        exists = itemCacheService.hasKey(1L).block();
        assertThat(exists).isFalse();

        Item cachedItem = itemCacheService.getItem(1L).block();
        assertThat(cachedItem).isNull();
    }

    @Test
    void getItem_ShouldReturnNull_ForNonExistentKey() {
        Item cachedItem = itemCacheService.getItem(999L).block();
        assertThat(cachedItem).isNull();
    }

    @Test
    void putItem_ShouldUpdateExistingItem() {
        // Сохраняем первый вариант товара
        itemCacheService.putItem(testItem).block();

        // Создаем обновленный товар
        Item updatedItem = new Item(1L, "Обновленный товар", "Новое описание", "images/new.jpg", 2000L, 0);

        // Сохраняем обновленный товар
        Boolean saved = itemCacheService.putItem(updatedItem).block();
        assertThat(saved).isTrue();

        // Получаем товар из кэша - должен быть обновленный
        Item cachedItem = itemCacheService.getItem(1L).block();
        assertThat(cachedItem).isNotNull();
        assertThat(cachedItem.getTitle()).isEqualTo("Обновленный товар");
        assertThat(cachedItem.getPrice()).isEqualTo(2000L);
    }

    @Test
    void hasKey_ShouldReturnFalse_ForNonExistentKey() {
        Boolean exists = itemCacheService.hasKey(999L).block();
        assertThat(exists).isFalse();
    }
}