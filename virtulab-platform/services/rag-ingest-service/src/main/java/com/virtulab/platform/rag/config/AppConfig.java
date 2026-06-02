package com.virtulab.platform.rag.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(RagProperties.class)
public class AppConfig {

    @Bean
    WebClient ollamaWebClient(RagProperties props) {
        return WebClient.builder().baseUrl(props.ollama().baseUrl()).build();
    }
}
