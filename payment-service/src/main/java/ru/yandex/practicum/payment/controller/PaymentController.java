package ru.yandex.practicum.payment.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.api.ApiApi;
import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.PaymentRequest;
import ru.yandex.practicum.payment.model.PaymentResponse;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
public class PaymentController implements ApiApi {

    private final AtomicLong balance;

    public PaymentController(@Value("${payment.initial-balance:100000}") long initialBalance) {
        this.balance = new AtomicLong(initialBalance);
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(ServerWebExchange exchange) {
        BalanceResponse response = new BalanceResponse();
        response.setBalance(balance.get());
        return Mono.just(ResponseEntity.ok(response));
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> processPayment(
            Mono<PaymentRequest> paymentRequest,
            ServerWebExchange exchange) {

        return paymentRequest.flatMap(request -> {
            long amount = request.getAmount();
            long currentBalance = balance.get();
            if (currentBalance >= amount) {
                long newBalance = currentBalance - amount;
                PaymentResponse response = new PaymentResponse();
                response.setSuccess(true);
                response.setNewBalance(newBalance);
                balance.set(newBalance);
                return Mono.just(ResponseEntity.ok(response));
            } else {
                return Mono.just(ResponseEntity.badRequest().build());
            }
        });
    }
}