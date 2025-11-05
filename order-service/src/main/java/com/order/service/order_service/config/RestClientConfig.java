package com.order.service.order_service.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.order.service.order_service.client.InventoryClient;
import com.order.service.order_service.client.PaymentClient;
import com.order.service.order_service.client.UserClient;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RestClientConfig {

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Bean
    public InventoryClient inventoryClient() {
        log.info("Creating InventoryClient with base URL: {}", inventoryServiceUrl);
        return createClient(inventoryServiceUrl, InventoryClient.class);
    }

    @Bean
    public PaymentClient paymentClient() {
        log.info("Creating PaymentClient with base URL: {}", paymentServiceUrl);
        return createClient(paymentServiceUrl, PaymentClient.class);
    }

    @Bean
    public UserClient userClient() {
        log.info("Creating UserClient with base URL: {}", userServiceUrl);
        return createClient(userServiceUrl, UserClient.class);
    }

    private <T> T createClient(String baseUrl, Class<T> clientClass) {
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(createRequestFactory())
                .build();

        var restClientAdapter = RestClientAdapter.create(restClient);
        var httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(restClientAdapter).build();
        return httpServiceProxyFactory.createClient(clientClass);
    }

    private JdkClientHttpRequestFactory createRequestFactory() {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(10));
        return factory;
    }
}
