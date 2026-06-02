package com.virtulab.platform.graphql.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    WebClient sessionServiceClient(
            WebClient.Builder builder,
            @Value("${virtulab.services.session.base-url}") String baseUrl
    ) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    WebClient eventsServiceClient(
            WebClient.Builder builder,
            @Value("${virtulab.services.events.base-url}") String baseUrl
    ) {
        return builder.baseUrl(baseUrl).build();
    }
}

