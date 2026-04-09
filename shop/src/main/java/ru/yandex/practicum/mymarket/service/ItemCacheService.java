package ru.yandex.practicum.mymarket.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;

import java.time.Duration;

@Service
public class ItemCacheService {

    private static final Logger log = LoggerFactory.getLogger(ItemCacheService.class);
    private static final String CACHE_KEY_PREFIX = "item:";

    @Autowired
    private ReactiveRedisTemplate<String, Item> reactiveRedisTemplate;

    private final Duration ttl;

    public ItemCacheService(
            ReactiveRedisTemplate<String, Item> reactiveRedisTemplate,
            @Value("${app.cache.item-ttl}") int ttlMinutes
    ) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.ttl = Duration.ofMinutes(ttlMinutes);
        log.info("ItemCacheService инициализирован с TTL: {} минут", ttlMinutes);
    }

    private String buildKey(Long id) {
        return CACHE_KEY_PREFIX + id;
    }

    public Mono<Item> getItem(Long id) {
        String key = buildKey(id);
        log.debug("Попытка получить товар из кэша по ключу: {}", key);

        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .doOnSuccess(item -> {
                    if (item != null) {
                        log.debug("Товар найден в кэше: id={}", id);
                    } else {
                        log.debug("Товар не найден в кэше: id={}", id);
                    }
                });
    }
    
    public Mono<Boolean> putItem(Item item) {
        if (item == null || item.getId() == null) {
            return Mono.just(false);
        }

        String key = buildKey(item.getId());
        log.debug("Сохранение товара в кэш: key={}, id={}", key, item.getId());

        return reactiveRedisTemplate.opsForValue()
                .set(key, item, ttl)
                .doOnSuccess(success -> {
                    if (success) {
                        log.debug("Товар успешно сохранен в кэше: id={}", item.getId());
                    } else {
                        log.warn("Не удалось сохранить товар в кэш: id={}", item.getId());
                    }
                });
    }

    public Mono<Boolean> deleteItem(Long id) {
        String key = buildKey(id);
        log.debug("Удаление товара из кэша: key={}, id={}", key, id);

        return reactiveRedisTemplate.delete(key)
                .map(deletedCount -> {
                    boolean success = deletedCount > 0;
                    if (success) {
                        log.debug("Товар успешно удален из кэша: id={}", id);
                    } else {
                        log.debug("Товар не найден в кэше для удаления: id={}", id);
                    }
                    return success;
                });
    }

    public Mono<Boolean> hasKey(Long id) {
        String key = buildKey(id);
        return reactiveRedisTemplate.hasKey(key);
    }

    public Mono<Boolean> expireItem(Long id) {
        String key = buildKey(id);
        return reactiveRedisTemplate.expire(key, ttl);
    }
}