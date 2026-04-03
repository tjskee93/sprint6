package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.model.dto.ItemDTO;
import ru.yandex.practicum.mymarket.model.dto.OrderDTO;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private CartService cartService;
    @Autowired
    private ItemRepository itemRepository;

    @Transactional
    public Mono<Order> createOrder() {
        return cartService.getCartItems()
                .collectList()
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        return Mono.error(new RuntimeException("Корзина пуста"));
                    }

                    Order order = new Order(LocalDateTime.now());

                    return orderRepository.save(order)
                            .flatMap(savedOrder ->
                                    Flux.fromIterable(cartItems)
                                            .flatMap(cartItem -> {
                                                OrderItem orderItem = new OrderItem(
                                                        savedOrder.getId(),
                                                        cartItem.id(),
                                                        cartItem.title(),
                                                        cartItem.price(),
                                                        cartItem.count()
                                                );
                                                return orderItemRepository.save(orderItem);
                                            })
                                            .collectList()
                                            .flatMap(savedItems -> {
                                                savedOrder.setItems(savedItems);
                                                return cartService.clearCart()
                                                        .thenReturn(savedOrder);
                                            })
                            );
                });
    }

    public Flux<OrderDTO> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc()
                .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                        .collectList()
                        .map(orderItems -> {
                            List<ItemDTO> items = orderItems.stream()
                                    .map(orderItem -> new ItemDTO(
                                            null,
                                            orderItem.getTitle(),
                                            null,
                                            null,
                                            orderItem.getPrice(),
                                            orderItem.getQuantity()
                                    ))
                                    .collect(Collectors.toList());
                            order.setItems(orderItems);
                            return new OrderDTO(order.getId(), items, order.getTotalSum());
                        })
                );
    }

    public Mono<OrderDTO> getOrderById(Long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Заказ не найден")))
                .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                        .collectList()
                        .map(orderItems -> {
                            List<ItemDTO> items = orderItems.stream()
                                    .map(orderItem -> new ItemDTO(
                                            null,
                                            orderItem.getTitle(),
                                            null,
                                            null,
                                            orderItem.getPrice(),
                                            orderItem.getQuantity()
                                    ))
                                    .collect(Collectors.toList());
                            order.setItems(orderItems);
                            return new OrderDTO(order.getId(), items, order.getTotalSum());
                        })
                );
    }
}