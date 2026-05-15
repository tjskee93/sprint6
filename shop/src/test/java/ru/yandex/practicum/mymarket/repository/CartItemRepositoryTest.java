package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.mymarket.MyMarketAppApplicationTests;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;

import static org.assertj.core.api.Assertions.assertThat;

class CartItemRepositoryTest extends MyMarketAppApplicationTests {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Long testItemId1;
    private Long testItemId2;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll().block();
        itemRepository.deleteAll().block();

        Item item1 = itemRepository.save(new Item(null, "Товар 1", "Описание 1", null, 100L, 0)).block();
        Item item2 = itemRepository.save(new Item(null, "Товар 2", "Описание 2", null, 200L, 0)).block();

        testItemId1 = item1.getId();
        testItemId2 = item2.getId();

        CartItem cartItem1 = new CartItem(1L, testItemId1, 2);
        CartItem cartItem2 = new CartItem(1L, testItemId2, 3);

        cartItemRepository.save(cartItem1).block();
        cartItemRepository.save(cartItem2).block();
    }

    @Test
    void findByItemId_ShouldReturnCartItem() {
        CartItem cartItem = cartItemRepository.findByUserIdAndItemId(1L, testItemId1).block();

        assertThat(cartItem).isNotNull();
        assertThat(cartItem.getItemId()).isEqualTo(testItemId1);
        assertThat(cartItem.getQuantity()).isEqualTo(2);
    }

    @Test
    void findAll_ShouldReturnAllCartItems() {
        Long count = cartItemRepository.findAll().count().block();

        assertThat(count).isEqualTo(2);
    }

    @Test
    void deleteByItemId_ShouldRemoveItemForProduct() {
        cartItemRepository.deleteByUserIdAndItemId(1L, testItemId1).block();

        Long count = cartItemRepository.findAll().count().block();

        assertThat(count).isEqualTo(1);
    }
}
