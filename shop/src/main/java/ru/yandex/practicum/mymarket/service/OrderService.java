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
    @Autowired
    private PaymentClientService paymentClientService;

    @Transactional
    public Mono<Order> createOrder(Long userId) {
        return cartService.getCartTotal(userId)
                .flatMap(total -> {
                    if (total == 0) {
                        return Mono.error(new RuntimeException("Корзина пуста"));
                    }
                    return Mono.just(total);
                })
                .flatMap(total -> paymentClientService.processPayment(total)
                        .flatMap(success -> {
                            if (!success) {
                                return Mono.error(new RuntimeException("Оплата не прошла. Недостаточно средств или сервис недоступен."));
                            }
                            return createOrderFromCart(userId);
                        })
                );
    }

    private Mono<Order> createOrderFromCart(Long userId) {
        return cartService.getCartItems(userId)
                .collectList()
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        return Mono.error(new RuntimeException("Корзина пуста"));
                    }

                    Order order = new Order(LocalDateTime.now());
                    order.setUserId(userId);

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
                                                return cartService.clearCart(userId)
                                                        .thenReturn(savedOrder);
                                            })
                            );
                });
    }

    public Flux<OrderDTO> getAllOrders(Long userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId)
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

    public Mono<OrderDTO> getOrderById(Long userId, Long orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new RuntimeException("Заказ не найден")))
                .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                        .collectList()
                        .map(orderItems -> {
                            List<ItemDTO> items = orderItems.stream()
                                    .map(orderItem -> new ItemDTO(
                                            orderItem.getItemId(),
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