package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.model.dto.ItemDTO;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {OrderService.class, CartService.class})
class OrderServiceTest {

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderItemRepository orderItemRepository;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderService orderService;

    private Item testItem;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testItem = new Item(1L, "Товар 1", "Описание 1", "images/1.jpg", 100L, 0);
        testOrder = new Order(1L, LocalDateTime.now(), null, null);
    }

    @Test
    void createOrder_WithItemsInCart_ShouldCreateOrder() {
        when(cartService.getCartItems()).thenReturn(Flux.just(ItemDTO.fromEntity(testItem)));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(testOrder));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(Mono.just(new OrderItem()));
        when(cartService.clearCart()).thenReturn(Mono.empty());

        Order result = orderService.createOrder().block();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartService, times(1)).clearCart();
    }

    @Test
    void createOrder_WithEmptyCart_ShouldReturnError() {
        when(cartService.getCartItems()).thenReturn(Flux.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder().block();
        });

        assertThat(exception.getMessage()).isEqualTo("Корзина пуста");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        Order order1 = new Order(1L, LocalDateTime.now(), null, null);

        when(orderRepository.findAllByOrderByOrderDateDesc()).thenReturn(Flux.just(order1));
        when(orderItemRepository.findByOrderId(anyLong())).thenReturn(Flux.empty());

        List<ru.yandex.practicum.mymarket.model.dto.OrderDTO> orders =
                orderService.getAllOrders().collectList().block();

        assertThat(orders).isNotNull();
        assertThat(orders.size()).isEqualTo(1);
    }

    @Test
    void getOrderById_WithNonExistentId_ShouldReturnError() {
        when(orderRepository.findById(999L)).thenReturn(Mono.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.getOrderById(999L).block();
        });

        assertThat(exception.getMessage()).isEqualTo("Заказ не найден");
    }
}