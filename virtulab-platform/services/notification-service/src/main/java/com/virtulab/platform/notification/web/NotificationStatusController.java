package com.virtulab.platform.notification.web;

import com.virtulab.platform.notification.messaging.NotificationConsumer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationStatusController {

    private final NotificationConsumer consumer;

    public NotificationStatusController(NotificationConsumer consumer) {
        this.consumer = consumer;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return Map.of(
                "processed", consumer.processedCount(),
                "queue", "q.notification.email");
    }
}
