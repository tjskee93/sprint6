package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;

@Repository
public interface ItemRepository extends R2dbcRepository<Item, Long> {
    @Query("SELECT * FROM items ORDER BY title ASC LIMIT :limit OFFSET :offset")
    Flux<Item> findAllByOrderByTitleAsc(int limit, int offset);
    @Query("SELECT * FROM items ORDER BY price ASC LIMIT :limit OFFSET :offset")
    Flux<Item> findAllByOrderByPriceAsc(int limit, int offset);
    @Query("SELECT * FROM items LIMIT :limit OFFSET :offset")
    Flux<Item> findAllWithPagination(int limit, int offset);
    @Query("SELECT * FROM items WHERE LOWER(title) LIKE CONCAT('%', LOWER(:search), '%') OR LOWER(description) LIKE CONCAT('%', LOWER(:search), '%') LIMIT :limit OFFSET :offset")
    Flux<Item> searchByTitleOrDescription(String search, int limit, int offset);
    @Query("SELECT * FROM items WHERE (LOWER(title) LIKE CONCAT('%', LOWER(:search), '%') OR LOWER(description) LIKE CONCAT('%', LOWER(:search), '%')) ORDER BY title ASC LIMIT :limit OFFSET :offset")
    Flux<Item> searchByTitleOrDescriptionOrderByTitleAsc(String search, int limit, int offset);
    @Query("SELECT * FROM items WHERE (LOWER(title) LIKE CONCAT('%', LOWER(:search), '%') OR LOWER(description) LIKE CONCAT('%', LOWER(:search), '%')) ORDER BY price ASC LIMIT :limit OFFSET :offset")
    Flux<Item> searchByTitleOrDescriptionOrderByPriceAsc(String search, int limit, int offset);
    // Подсчет количества
    @Query("SELECT COUNT(*) FROM items WHERE LOWER(title) LIKE CONCAT('%', LOWER(:search), '%') OR LOWER(description) LIKE CONCAT('%', LOWER(:search), '%')")
    Mono<Long> countBySearch(String search);

}