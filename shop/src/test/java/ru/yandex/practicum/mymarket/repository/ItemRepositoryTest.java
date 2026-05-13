package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.mymarket.MyMarketAppApplicationTests;
import ru.yandex.practicum.mymarket.model.Item;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ItemRepositoryTest extends MyMarketAppApplicationTests {

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll().block();

        itemRepository.save(new Item(null, "Яблоко", "Сладкое яблоко", "images/apple.jpg", 100L, 0)).block();
        itemRepository.save(new Item(null, "Банан", "Спелый банан", "images/banana.jpg", 80L, 0)).block();
        itemRepository.save(new Item(null, "Апельсин", "Сочный апельсин", "images/orange.jpg", 120L, 0)).block();
    }

    @Test
    void findAllByOrderByPriceAsc_ShouldReturnSortedByPrice() {
        List<Long> prices = itemRepository.findAllByOrderByPriceAsc(10, 0)
                .map(Item::getPrice)
                .collectList()
                .block();

        assertThat(prices).isEqualTo(List.of(80L, 100L, 120L));
    }

    @Test
    void findAllWithPagination_ShouldReturnLimitedItems() {
        List<Item> items = itemRepository.findAllWithPagination(2, 0)
                .collectList()
                .block();

        assertThat(items.size()).isEqualTo(2);
        assertThat(items.get(0).getTitle()).isEqualTo("Яблоко");
        assertThat(items.get(1).getTitle()).isEqualTo("Банан");
    }

    @Test
    void searchByTitleOrDescription_ShouldReturnMatchingItems() {
        List<String> titles = itemRepository.searchByTitleOrDescription("апель", 10, 0)
                .map(Item::getTitle)
                .collectList()
                .block();

        assertThat(titles.size()).isEqualTo(1);
        assertThat(titles.get(0)).isEqualTo("Апельсин");
    }
}