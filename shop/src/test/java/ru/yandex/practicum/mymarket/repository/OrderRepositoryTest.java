package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.mymarket.MyMarketAppApplicationTests;
import ru.yandex.practicum.mymarket.model.Order;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRepositoryTest extends MyMarketAppApplicationTests {

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll().block();

        LocalDateTime now = LocalDateTime.now();
        orderRepository.save(new Order(null, now.minusDays(2), null, null)).block();
        orderRepository.save(new Order(null, now.minusDays(1), null, null)).block();
        orderRepository.save(new Order(null, now, null, null)).block();
    }

    @Test
    void findAllByOrderByOrderDateDesc_ShouldReturnOrdersSortedDescending() {
        List<LocalDateTime> dates = orderRepository.findAllByOrderByOrderDateDesc()
                .map(Order::getOrderDate)
                .collectList()
                .block();

        assertThat(dates.get(0)).isAfter(dates.get(1));
        assertThat(dates.get(1)).isAfter(dates.get(2));
    }

    @Test
    void save_ShouldCreateNewOrder() {
        Order newOrder = new Order(null, LocalDateTime.now(), null, null);

        Order saved = orderRepository.save(newOrder).block();

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void count_ShouldReturnTotalNumberOfOrders() {
        Long count = orderRepository.count().block();

        assertThat(count).isEqualTo(3);
    }
}
