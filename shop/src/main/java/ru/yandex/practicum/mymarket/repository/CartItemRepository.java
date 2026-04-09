package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.CartItem;

@Repository
public interface CartItemRepository extends R2dbcRepository<CartItem, Long> {
    Mono<CartItem> findByItemId(Long itemId);
    Mono<Void> deleteByItemId(Long itemId);
}