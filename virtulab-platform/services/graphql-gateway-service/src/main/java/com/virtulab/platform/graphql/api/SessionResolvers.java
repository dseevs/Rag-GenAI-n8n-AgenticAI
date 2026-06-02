package com.virtulab.platform.graphql.api;

import com.virtulab.platform.contracts.events.EventAcceptedResponse;
import com.virtulab.platform.contracts.events.ProgressEventRequest;
import com.virtulab.platform.contracts.session.SessionResponse;
import com.virtulab.platform.contracts.session.StartSessionRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Controller
public class SessionResolvers {

    private final WebClient sessionClient;
    private final WebClient eventsClient;

    public SessionResolvers(
            @Qualifier("sessionServiceClient") WebClient sessionServiceClient,
            @Qualifier("eventsServiceClient") WebClient eventsServiceClient
    ) {
        this.sessionClient = sessionServiceClient;
        this.eventsClient = eventsServiceClient;
    }

    @QueryMapping
    public Mono<SessionResponse> session(
            @Argument("attemptId") String attemptId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return sessionClient.get()
                .uri("/api/v1/sessions/{attemptId}", attemptId)
                .headers(h -> h.setBearerAuth(jwt.getTokenValue()))
                .retrieve()
                .bodyToMono(SessionResponse.class);
    }

    @MutationMapping
    public Mono<SessionResponse> startSession(
            @Argument("input") StartSessionInput input,
            @AuthenticationPrincipal Jwt jwt
    ) {
        StartSessionRequest req = new StartSessionRequest(input.experimentId(), input.attemptId(), input.mode(), input.lang());
        return sessionClient.post()
                .uri("/api/v1/sessions")
                .headers(h -> h.setBearerAuth(jwt.getTokenValue()))
                .bodyValue(req)
                .retrieve()
                .bodyToMono(SessionResponse.class);
    }

    @MutationMapping
    public Mono<EventAcceptedResponse> recordProgress(
            @Argument("input") ProgressInput input,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ProgressEventRequest req = new ProgressEventRequest(
                input.experimentId(),
                input.attemptId(),
                input.stepId(),
                input.timeSpentSec(),
                input.progress()
        );

        return eventsClient.post()
                .uri("/api/v1/events/progress")
                .headers(h -> h.setBearerAuth(jwt.getTokenValue()))
                .bodyValue(req)
                .retrieve()
                .bodyToMono(EventAcceptedResponse.class);
    }

    public record StartSessionInput(String experimentId, String attemptId, String mode, String lang) {}

    public record ProgressInput(String experimentId, String attemptId, String stepId, Integer timeSpentSec, java.util.Map<String, Object> progress) {}
}

