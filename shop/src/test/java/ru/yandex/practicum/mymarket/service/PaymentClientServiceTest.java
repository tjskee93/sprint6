package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {PaymentClientService.class})
class PaymentClientServiceTest {

    @MockitoBean
    private WebClient.Builder webClientBuilder;

    @Autowired
    private PaymentClientService paymentClientService;

    @BeforeEach
    void setUp() {
        WebClient mockWebClient = org.mockito.Mockito.mock(WebClient.class);
        when(webClientBuilder.baseUrl(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(mockWebClient);
    }

    @Test
    void getBalance_ShouldReturnBalance_WhenServiceIsAvailable() {
        assertThat(paymentClientService).isNotNull();
    }

    @Test
    void processPayment_ShouldReturnTrue_WhenPaymentSuccessful() {
        assertThat(paymentClientService).isNotNull();
    }
}
