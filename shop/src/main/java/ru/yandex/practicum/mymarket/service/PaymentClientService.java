package ru.yandex.practicum.mymarket.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.client.payment.api.ApiApi;
import ru.yandex.practicum.shop.client.payment.model.BalanceResponse;
import ru.yandex.practicum.shop.client.payment.model.PaymentRequest;
import ru.yandex.practicum.shop.client.payment.model.PaymentResponse;

@Service
public class PaymentClientService {

    private final WebClient webClient;
    public PaymentClientService(@Qualifier("paymentWebClient") WebClient paymentWebClient) {
        this.webClient = paymentWebClient;
    }

    public Mono<Long> getBalance() {
        return webClient.get()
                .uri("/api/balance")
                .retrieve()
                .bodyToMono(BalanceResponse.class)
                .map(BalanceResponse::getBalance)
                .onErrorReturn(-1L);
    }

    public Mono<Boolean> processPayment(long amount) {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(amount);

        return webClient.post()
                .uri("/api/payment")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .map(PaymentResponse::getSuccess)
                .onErrorReturn(false);
    }
}