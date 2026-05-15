package ru.yandex.practicum.mymarket.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.User;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(ItemRepository itemRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        // Создаем тестового пользователя, если его нет
        userRepository.findByUsername("testuser")
                .switchIfEmpty(Mono.defer(() -> {
                    User user = User.builder()
                            .username("testuser")
                            .password(passwordEncoder.encode("password"))
                            .enabled(true)
                            .build();
                    return userRepository.save(user);
                }))
                .subscribe(user -> System.out.println("Пользователь testuser готов"));
        userRepository.findByUsername("testuser2")
                .switchIfEmpty(Mono.defer(() -> {
                    User user = User.builder()
                            .username("testuser2")
                            .password(passwordEncoder.encode("password"))
                            .enabled(true)
                            .build();
                    return userRepository.save(user);
                }))
                .subscribe(user -> System.out.println("Пользователь testuser2 готов"));
    }

    private Flux<Item> createInitialItems() {
        return Flux.just(
                new Item("Футболка", "Классическая белая футболка.", "/images/tshirt.jpg", 1999L),
                new Item("Джинсы", "Современные джинсы темно-синего цвета.", "/images/jeans.jpg", 3499L),
                new Item("Кроссовки для бега", "Идеально подходят для ежедневных пробежек.", "/images/shoes.jpg", 4999L),
                new Item("Шерстяной свитер", "Теплый шерстяной свитер.", "/images/sweater.jpg", 3999L),
                new Item("Кожаная куртка", "Вечный стиль.", "/images/jacket.jpg", 8999L)
        );
    }
}