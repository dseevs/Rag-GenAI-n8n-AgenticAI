package com.virtulab.platform.events.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqProducerConfig {

    @Bean
    TopicExchange virtulabTasksExchange(@Value("${virtulab.rabbitmq.exchange:virtulab.tasks}") String name) {
        return new TopicExchange(name);
    }

    @Bean
    Queue notificationQueue(@Value("${virtulab.rabbitmq.queue.notification:q.notification.email}") String name) {
        return new Queue(name, true);
    }

    @Bean
    Binding notificationBinding(
            Queue notificationQueue,
            TopicExchange virtulabTasksExchange,
            @Value("${virtulab.rabbitmq.routing-key.notification:notification.email}") String routingKey
    ) {
        return BindingBuilder.bind(notificationQueue).to(virtulabTasksExchange).with(routingKey);
    }
}
