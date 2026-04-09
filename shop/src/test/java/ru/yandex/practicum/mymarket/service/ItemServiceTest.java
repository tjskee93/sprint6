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
import ru.yandex.practicum.mymarket.model.dto.ItemDTO;
import ru.yandex.practicum.mymarket.model.dto.PagingDTO;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {ItemService.class, CartService.class, ItemCacheService.class})
class ItemServiceTest {

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private CartItemRepository cartItemRepository;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private ItemCacheService itemCacheService;

    @Autowired
    private ItemService itemService;

    private Item testItem;

    @BeforeEach
    void setUp() {
        testItem = new Item(1L, "Тестовый товар", "Описание", "images/test.jpg", 1000L, 0);
    }

    @Test
    void getItemById_ShouldReturnFromCache_WhenItemExistsInCache() {
        when(itemCacheService.getItem(1L)).thenReturn(Mono.just(testItem));

        Item result = itemService.getItemById(1L).block();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(itemCacheService, times(1)).getItem(1L);
        verify(itemRepository, never()).findById(anyLong());
    }

    @Test
    void getItemById_ShouldReturnFromDatabaseAndCache_WhenNotInCache() {
        when(itemCacheService.getItem(1L)).thenReturn(Mono.empty());
        when(itemRepository.findById(1L)).thenReturn(Mono.just(testItem));
        when(itemCacheService.putItem(testItem)).thenReturn(Mono.just(true));

        Item result = itemService.getItemById(1L).block();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(itemCacheService, times(1)).getItem(1L);
        verify(itemRepository, times(1)).findById(1L);
        verify(itemCacheService, times(1)).putItem(testItem);
    }

    @Test
    void getItemById_WithNonExistentId_ShouldReturnError() {
        when(itemCacheService.getItem(999L)).thenReturn(Mono.empty());
        when(itemRepository.findById(999L)).thenReturn(Mono.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            itemService.getItemById(999L).block();
        });

        assertThat(exception.getMessage()).contains("Товар не найден");
    }

    @Test
    void getItems_ShouldReturnPaginatedItems() {
        when(itemRepository.findAllWithPagination(3, 0))
                .thenReturn(Flux.just(testItem));
        when(cartItemRepository.findByItemId(anyLong()))
                .thenReturn(Mono.empty());

        var result = itemService.getItems(null, "NO", 1, 3).block();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void getItems_WithSearch_ShouldReturnFilteredItems() {
        when(itemRepository.searchByTitleOrDescription(eq("тест"), eq(10), eq(0)))
                .thenReturn(Flux.just(testItem));
        when(cartItemRepository.findByItemId(anyLong()))
                .thenReturn(Mono.empty());

        var result = itemService.getItems("тест", "NO", 1, 10).block();

        assertThat(result).isNotNull();
        long count = result.stream()
                .flatMap(List::stream)
                .filter(item -> item.id() != -1)
                .count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void getPagingInfo_ShouldReturnCorrectPagination() {
        when(itemRepository.count()).thenReturn(Mono.just(10L));

        PagingDTO paging = itemService.getPagingInfo(null, "NO", 2, 3).block();

        assertThat(paging).isNotNull();
        assertThat(paging.pageSize()).isEqualTo(3);
        assertThat(paging.pageNumber()).isEqualTo(2);
        assertThat(paging.totalElements()).isEqualTo(10);
        assertThat(paging.totalPages()).isEqualTo(4);
        assertThat(paging.hasPrevious()).isTrue();
        assertThat(paging.hasNext()).isTrue();
    }

    @Test
    void updateCart_ShouldEvictCache() {
        when(cartService.addToCart(1L)).thenReturn(Mono.empty());
        when(itemCacheService.deleteItem(1L)).thenReturn(Mono.just(true));

        itemService.updateCart(1L, "PLUS").block();

        verify(cartService, times(1)).addToCart(1L);
        verify(itemCacheService, times(1)).deleteItem(1L);
    }

    @Test
    void evictItemCache_ShouldDeleteFromCache() {
        when(itemCacheService.deleteItem(1L)).thenReturn(Mono.just(true));

        itemService.evictItemCache(1L).block();

        verify(itemCacheService, times(1)).deleteItem(1L);
    }

    @Test
    void getItemWithCartCount_ShouldReturnItemWithCountFromCart() {
        CartItem cartItem = new CartItem(1L, 3);

        when(itemCacheService.getItem(1L)).thenReturn(Mono.just(testItem));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.just(cartItem));

        ItemDTO result = itemService.getItemWithCartCount(1L).block();

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.count()).isEqualTo(3);
    }
}