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
    public Mono<Void> addToCart(Long itemId) {
        return itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new RuntimeException("Товар не найден")))
                .flatMap(item -> cartItemRepository.findByItemId(itemId)
                        .flatMap(cartItem -> {
                            cartItem.setQuantity(cartItem.getQuantity() + 1);
                            return cartItemRepository.save(cartItem);
                        })
                        .switchIfEmpty(Mono.defer(() ->
                                cartItemRepository.save(new CartItem(itemId, 1))
                        ))
                )
                .then();
    }

    @Transactional
    public Mono<Void> removeFromCart(Long itemId) {
        return cartItemRepository.findByItemId(itemId)
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
    public Mono<Void> deleteFromCart(Long itemId) {
        return cartItemRepository.deleteByItemId(itemId);
    }

    public Flux<ItemDTO> getCartItems() {
        return cartItemRepository.findAll()
                .flatMap(cartItem -> itemRepository.findById(cartItem.getItemId())
                        .map(item -> {
                            item.setCount(cartItem.getQuantity());
                            return ItemDTO.fromEntity(item);
                        })
                );
    }

    public Mono<Long> getCartTotal() {
        return getCartItems()
                .map(item -> item.price() * item.count())
                .reduce(0L, Long::sum);
    }

    @Transactional
    public Mono<Void> clearCart() {
        return cartItemRepository.deleteAll();
    }
}