package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.CartItem;

@Repository
public interface CartItemRepository extends R2dbcRepository<CartItem, Long> {
    Flux<CartItem> findByUserId(Long userId);
    Mono<CartItem> findByUserIdAndItemId(Long userId, Long itemId);
    Mono<Void> deleteByUserId(Long userId);
    Mono<Void> deleteByUserIdAndItemId(Long userId, Long itemId);
}