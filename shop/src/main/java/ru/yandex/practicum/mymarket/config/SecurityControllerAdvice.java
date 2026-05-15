package ru.yandex.practicum.mymarket.config;

import org.springframework.core.annotation.Order;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@ControllerAdvice
@Order(0)
public class SecurityControllerAdvice {

    @ModelAttribute("_csrf")
    public Mono<CsrfToken> csrfToken(ServerWebExchange exchange) {
        return exchange.getAttributeOrDefault(CsrfToken.class.getName(), Mono.empty());
    }
}
