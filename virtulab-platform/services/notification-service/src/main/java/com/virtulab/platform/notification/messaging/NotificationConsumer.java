package com.virtulab.platform.notification.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private final AtomicLong processed = new AtomicLong();

    @RabbitListener(queues = "${virtulab.rabbitmq.queue.notification:q.notification.email}")
    public void onNotification(String payload) {
        long count = processed.incrementAndGet();
        log.info("[notification #{}] would send email/push: {}", count, payload);
    }

    public long processedCount() {
        return processed.get();
    }
}
