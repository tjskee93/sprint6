package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.mymarket.MyMarketAppApplicationTests;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemRepositoryTest extends MyMarketAppApplicationTests {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Long testOrderId;
    private Long testItemId;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll().block();
        orderRepository.deleteAll().block();
        itemRepository.deleteAll().block();

        Item item = itemRepository.save(new Item(null, "Товар", "Описание", null, 100L, 0)).block();
        testItemId = item.getId();

        Order order = orderRepository.save(new Order(null, null, LocalDateTime.now(), null, null)).block();
        testOrderId = order.getId();

        OrderItem orderItem = new OrderItem(testOrderId, testItemId, "Товар", 100L, 2);
        orderItemRepository.save(orderItem).block();
    }

    @Test
    void findByOrderId_ShouldReturnOrderItems() {
        List<OrderItem> items = orderItemRepository.findByOrderId(testOrderId).collectList().block();

        assertThat(items).hasSize(1);
    }

    @Test
    void save_ShouldCreateNewOrderItem() {
        Item newItem = itemRepository.save(new Item(null, "Новый товар", "Описание", null, 150L, 0)).block();

        OrderItem newOrderItem = new OrderItem(testOrderId, newItem.getId(), "Новый товар", 150L, 3);

        OrderItem saved = orderItemRepository.save(newOrderItem).block();

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getQuantity()).isEqualTo(3);
    }

    @Test
    void findAll_ShouldReturnAllOrderItems() {
        Long count = orderItemRepository.findAll().count().block();

        assertThat(count).isEqualTo(1);
    }
}
