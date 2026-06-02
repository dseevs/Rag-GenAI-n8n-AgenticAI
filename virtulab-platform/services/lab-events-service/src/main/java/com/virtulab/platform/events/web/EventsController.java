package com.virtulab.platform.events.web;

import com.virtulab.platform.contracts.events.EventAcceptedResponse;
import com.virtulab.platform.contracts.events.ProgressEventRequest;
import com.virtulab.platform.events.security.AuthContext;
import com.virtulab.platform.events.service.ProgressEventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/events")
public class EventsController {

    private final ProgressEventService progressEventService;

    public EventsController(ProgressEventService progressEventService) {
        this.progressEventService = progressEventService;
    }

    @PostMapping("/progress")
    public Mono<ResponseEntity<EventAcceptedResponse>> progress(
            @Valid @RequestBody ProgressEventRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return progressEventService.ingest(request, AuthContext.from(jwt), idempotencyKey)
                .map(body -> ResponseEntity.status(HttpStatus.ACCEPTED).body(body));
    }
}
