package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.dto.PagingDTO;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ItemService.class, CartService.class})
class ItemServiceTest {

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private CartItemRepository cartItemRepository;

    @MockitoBean
    private CartService cartService;

    @Autowired
    private ItemService itemService;

    private Item testItem;

    @BeforeEach
    void setUp() {
        testItem = new Item(1L, "Тестовый товар", "Описание", "images/test.jpg", 1000L, 0);
    }

    @Test
    void getItems_ShouldReturnPaginatedItems() {
        when(itemRepository.findAllWithPagination(3, 0))
                .thenReturn(reactor.core.publisher.Flux.just(testItem));
        when(cartItemRepository.findByItemId(anyLong()))
                .thenReturn(reactor.core.publisher.Mono.empty());

        var result = itemService.getItems(null, "NO", 1, 3).block();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void getItems_WithSearch_ShouldReturnFilteredItems() {
        when(itemRepository.searchByTitleOrDescription(eq("тест"), eq(10), eq(0)))
                .thenReturn(reactor.core.publisher.Flux.just(testItem));
        when(cartItemRepository.findByItemId(anyLong()))
                .thenReturn(reactor.core.publisher.Mono.empty());

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
        when(itemRepository.count()).thenReturn(reactor.core.publisher.Mono.just(10L));

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
    void getItemById_WithNonExistentId_ShouldReturnError() {
        when(itemRepository.findById(999L)).thenReturn(reactor.core.publisher.Mono.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            itemService.getItemById(999L).block();
        });

        assertThat(exception.getMessage()).isEqualTo("Товар не найден");
    }
}
