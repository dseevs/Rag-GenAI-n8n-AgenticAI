package com.virtulab.platform.session.config;

import com.virtulab.platform.contracts.security.DevJwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    DevJwtService devJwtService(@Value("${virtulab.jwt.secret}") String secret) {
        return new DevJwtService(secret);
    }
}
