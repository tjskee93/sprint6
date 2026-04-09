package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = CartService.class)
class CartServiceTest {

    @MockitoBean
    private CartItemRepository cartItemRepository;

    @MockitoBean
    private ItemRepository itemRepository;

    @Autowired
    private CartService cartService;

    private Item testItem;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testItem = new Item(1L, "Товар", "Описание", "images/test.jpg", 100L, 0);
        testCartItem = new CartItem(1L, 2);
    }

    @Test
    void addToCart_WithNewItem_ShouldCreateNewCartItem() {
        when(itemRepository.findById(1L)).thenReturn(Mono.just(testItem));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(new CartItem(1L, 1)));

        cartService.addToCart(1L).block();

        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addToCart_WithNonExistentItem_ShouldReturnError() {
        when(itemRepository.findById(999L)).thenReturn(Mono.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.addToCart(999L).block();
        });

        assertThat(exception.getMessage()).isEqualTo("Товар не найден");
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void getCartTotal_ShouldReturnSumOfAllItems() {
        CartItem cartItem1 = new CartItem(1L, 2);
        CartItem cartItem2 = new CartItem(2L, 1);
        Item item1 = new Item(1L, "Товар 1", "Описание 1", "images/1.jpg", 100L, 0);
        Item item2 = new Item(2L, "Товар 2", "Описание 2", "images/2.jpg", 200L, 0);

        when(cartItemRepository.findAll()).thenReturn(Flux.just(cartItem1, cartItem2));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item1));
        when(itemRepository.findById(2L)).thenReturn(Mono.just(item2));

        Long total = cartService.getCartTotal().block();

        assertThat(total).isEqualTo(400L);
    }

    @Test
    void clearCart_ShouldDeleteAllItems() {
        when(cartItemRepository.deleteAll()).thenReturn(Mono.empty());

        cartService.clearCart().block();

        verify(cartItemRepository, times(1)).deleteAll();
    }
}