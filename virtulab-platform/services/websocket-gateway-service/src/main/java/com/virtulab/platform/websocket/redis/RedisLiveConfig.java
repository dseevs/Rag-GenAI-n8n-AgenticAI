package com.virtulab.platform.websocket.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import reactor.core.publisher.Flux;

@Configuration
public class RedisLiveConfig {

    @Bean
    ReactiveRedisMessageListenerContainer reactiveRedisMessageListenerContainer(
            ReactiveRedisConnectionFactory factory
    ) {
        return new ReactiveRedisMessageListenerContainer(factory);
    }

    @Bean
    Flux<String> liveProgressFlux(
            ReactiveRedisMessageListenerContainer container,
            @Value("${virtulab.ws.redis-channel:virtulab:live:progress}") String channel
    ) {
        return container.receive(ChannelTopic.of(channel))
                .map(message -> message.getMessage());
    }
}
