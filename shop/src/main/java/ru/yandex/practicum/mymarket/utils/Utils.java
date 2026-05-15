package ru.yandex.practicum.mymarket.utils;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;

public class Utils {
    public static Mono<String> getCurrentUserName() {
    return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMap(auth -> {
                if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                    return Mono.just(""); // Анонимный пользователь
                }
                return Mono.just(auth.getName());
            })
            .defaultIfEmpty("");
    }
}
