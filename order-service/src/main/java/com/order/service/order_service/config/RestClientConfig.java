package com.order.service.order_service.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Web.Client;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.order.service.order_service.client.InventoryClient;

@Configuration
public class RestClientConfig {

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    @Bean
    public InventoryClient inventoryClient () { 
        RestClient restClient = RestClient.builder()
        .baseUrl(inventoryServiceUrl)
        // .requestFactory(getClientRequestFactory())
        .build();
        var restClientAdaptor = RestClientAdapter.create(restClient);
        var httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(restClientAdaptor).build();
        return httpServiceProxyFactory.createClient(InventoryClient.class);
    }

    // private ClientHttpRequestFactory getClientRequestFactory() {
    //     ClientHttpRequestFactorySettings clientHttpRequestFactorySettings = ClientHttpRequestFactorySettings.defaults()
    //     .withConnectTimeout(Duration.ofSeconds(3))
    //     .withReadTimeout(Duration.ofSeconds(3));
    //     return ClientHttpRequestFactories.get(clientHttpRequestFactorySettings);
    // }
}

