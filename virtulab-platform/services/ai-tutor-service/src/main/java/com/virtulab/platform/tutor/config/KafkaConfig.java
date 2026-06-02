package com.virtulab.platform.tutor.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    NewTopic labAiTopic(@Value("${virtulab.kafka.topic.ai}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }
}
