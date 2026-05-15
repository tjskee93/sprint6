package ru.yandex.practicum.mymarket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.User;
import ru.yandex.practicum.mymarket.repository.UserRepository;

@Service
public class UserService  {
    @Autowired
    private UserRepository userRepository;

    public Mono<Long> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> {
                    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                        return Mono.just(0L); // Анонимный пользователь
                    }
                    return userRepository.findByUsername(auth.getName())
                            .map(User::getId)
                            .defaultIfEmpty(0L);
                })
                .defaultIfEmpty(0L);
    }
}
