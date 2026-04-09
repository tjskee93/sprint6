package ru.yandex.practicum.mymarket.service;

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
public class PaymentClientService implements ApiApi {

    private final WebClient webClient;

    public PaymentClientService(@Value("${payment.service.url}") String paymentUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(paymentUrl)
                .build();
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(ServerWebExchange exchange) {
        return webClient.get()
                .uri("/api/balance")
                .retrieve()
                .bodyToMono(BalanceResponse.class)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    return Mono.just(ResponseEntity.status(503).build());
                });
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> processPayment(Mono<PaymentRequest> paymentRequest, ServerWebExchange exchange) {
        return paymentRequest.flatMap(request ->
                webClient.post()
                        .uri("/api/payment")
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(PaymentResponse.class)
                        .map(ResponseEntity::ok)
                        .onErrorResume(e -> {
                            return Mono.just(ResponseEntity.status(503).build());
                        })
        );
    }

    public Mono<Long> getBalance() {
        return getBalance(null)
                .map(response -> {
                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        return response.getBody().getBalance();
                    }
                    return -1L;
                });
    }

    public Mono<Boolean> processPayment(long amount) {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(amount);

        return processPayment(Mono.just(request), null)
                .map(response -> {
                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        return response.getBody().getSuccess();
                    }
                    return false;
                });
    }
}