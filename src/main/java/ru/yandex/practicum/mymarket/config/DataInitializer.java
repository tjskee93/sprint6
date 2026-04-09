package ru.yandex.practicum.mymarket.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ItemRepository itemRepository;

    public DataInitializer(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public void run(String... args) {
        itemRepository.count()
                .filter(count -> count == 0)
                .flatMapMany(count -> {
                    return createInitialItems();
                })
                .flatMap(itemRepository::save)
                .subscribe();
    }

    private Flux<Item> createInitialItems() {
        return Flux.just(
                new Item("Футболка", "Классическая белая футболка.", "images/tshirt.jpg", 1999L),
                new Item("Джинсы", "Современные джинсы темно-синего цвета.", "images/jeans.jpg", 3499L),
                new Item("Кроссовки для бега", "Идеально подходят для ежедневных пробежек.", "images/shoes.jpg", 4999L),
                new Item("Шерстяной свитер", "Теплый шерстяной свитер.", "images/sweater.jpg", 3999L),
                new Item("Кожаная куртка", "Вечный стиль.", "images/jacket.jpg", 8999L)
        );
    }
}