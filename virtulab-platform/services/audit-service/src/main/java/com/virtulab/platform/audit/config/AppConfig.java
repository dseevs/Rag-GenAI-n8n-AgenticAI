package com.virtulab.platform.audit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean(initMethod = "migrate")
    Flyway flyway(
            @Value("${virtulab.flyway.url}") String url,
            @Value("${virtulab.flyway.user}") String user,
            @Value("${virtulab.flyway.password}") String password
    ) {
        return Flyway.configure()
                .dataSource(url, user, password)
                .schemas("audit")
                .locations("classpath:db/migration")
                .load();
    }
}
