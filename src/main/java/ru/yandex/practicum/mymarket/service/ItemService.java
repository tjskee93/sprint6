package ru.yandex.practicum.mymarket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.dto.ItemDTO;
import ru.yandex.practicum.mymarket.model.dto.PagingDTO;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private CartService cartService;

    public Mono<List<List<ItemDTO>>> getItems(String search, String sort, int pageNumber, int pageSize) {
        int offset = (pageNumber - 1) * pageSize;

        Flux<Item> itemsFlux;

        if (search != null && !search.trim().isEmpty()) {
            String searchTerm = search.trim();
            switch (sort) {
                case "ALPHA":
                    itemsFlux = itemRepository.searchByTitleOrDescriptionOrderByTitleAsc(searchTerm, pageSize, offset);
                    break;
                case "PRICE":
                    itemsFlux = itemRepository.searchByTitleOrDescriptionOrderByPriceAsc(searchTerm, pageSize, offset);
                    break;
                default:
                    itemsFlux = itemRepository.searchByTitleOrDescription(searchTerm, pageSize, offset);
            }
        } else {
            switch (sort) {
                case "ALPHA":
                    itemsFlux = itemRepository.findAllByOrderByTitleAsc(pageSize, offset);
                    break;
                case "PRICE":
                    itemsFlux = itemRepository.findAllByOrderByPriceAsc(pageSize, offset);
                    break;
                default:
                    itemsFlux = itemRepository.findAllWithPagination(pageSize, offset);
            }
        }

        return itemsFlux
                .flatMap(item -> cartItemRepository.findByItemId(item.getId())
                        .map(cartItem -> {
                            item.setCount(cartItem.getQuantity());
                            return item;
                        })
                        .defaultIfEmpty(item)
                )
                .map(ItemDTO::fromEntity)
                .collectList()
                .map(this::toRows);
    }

    private List<List<ItemDTO>> toRows(List<ItemDTO> items) {
        List<List<ItemDTO>> rows = new ArrayList<>();
        for (int i = 0; i < items.size(); i += 3) {
            List<ItemDTO> row = new ArrayList<>();
            for (int j = i; j < i + 3 && j < items.size(); j++) {
                row.add(items.get(j));
            }
            while (row.size() < 3) {
                row.add(new ItemDTO(-1L, "", "", "", 0L, 0));
            }
            rows.add(row);
        }
        return rows;
    }

    public Mono<PagingDTO> getPagingInfo(String search, String sort, int pageNumber, int pageSize) {
        Mono<Long> countMono;
        if (search != null && !search.trim().isEmpty()) {
            String searchTerm = search.trim();
            countMono = itemRepository.countBySearch(searchTerm);
        } else {
            countMono = itemRepository.count();
        }

        return countMono.map(totalElements -> {
            long totalPages = (totalElements + pageSize - 1) / pageSize;
            return new PagingDTO(
                    pageSize,
                    pageNumber,
                    pageNumber > 1,
                    pageNumber < totalPages,
                    totalPages,
                    totalElements
            );
        });
    }

    public Mono<ItemDTO> getItemById(Long id) {
        return itemRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Товар не найден")))
                .flatMap(item -> cartItemRepository.findByItemId(item.getId())
                        .map(cartItem -> {
                            item.setCount(cartItem.getQuantity());
                            return item;
                        })
                        .defaultIfEmpty(item)
                )
                .map(ItemDTO::fromEntity);
    }

    @Transactional
    public Mono<Void> updateCart(Long id, String action) {
        return switch (action) {
            case "PLUS" -> cartService.addToCart(id);
            case "MINUS" -> cartService.removeFromCart(id);
            default -> Mono.empty();
        };
    }
}