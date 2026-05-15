package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.dto.ItemDTO;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class CartService {
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ItemRepository itemRepository;

    @Transactional
    public Mono<Void> addToCart(Long userId, Long itemId) {
        return itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new RuntimeException("Товар не найден")))
                .flatMap(item -> cartItemRepository.findByUserIdAndItemId(userId, itemId)
                        .flatMap(cartItem -> {
                            cartItem.setQuantity(cartItem.getQuantity() + 1);
                            return cartItemRepository.save(cartItem);
                        })
                        .switchIfEmpty(Mono.defer(() ->
                                cartItemRepository.save(new CartItem(userId, itemId, 1))
                        ))
                )
                .then();
    }

    @Transactional
    public Mono<Void> removeFromCart(Long userId, Long itemId) {
        return cartItemRepository.findByUserIdAndItemId(userId, itemId)
                .flatMap(cartItem -> {
                    if (cartItem.getQuantity() > 1) {
                        cartItem.setQuantity(cartItem.getQuantity() - 1);
                        return cartItemRepository.save(cartItem);
                    } else {
                        return cartItemRepository.deleteById(cartItem.getId());
                    }
                })
                .then();
    }

    @Transactional
    public Mono<Void> deleteFromCart(Long userId, Long itemId) {
        return cartItemRepository.deleteByUserIdAndItemId(userId, itemId);
    }

    public Flux<ItemDTO> getCartItems(Long userId) {
        return cartItemRepository.findByUserId(userId)
                .flatMap(cartItem -> itemRepository.findById(cartItem.getItemId())
                        .map(item -> {
                            item.setCount(cartItem.getQuantity());
                            return ItemDTO.fromEntity(item);
                        })
                );
    }

    public Mono<Long> getCartTotal(Long userId) {
        return getCartItems(userId)
                .map(item -> item.price() * item.count())
                .reduce(0L, Long::sum);
    }

    @Transactional
    public Mono<Void> clearCart(Long userId) {
        return cartItemRepository.deleteByUserId(userId);
    }
}