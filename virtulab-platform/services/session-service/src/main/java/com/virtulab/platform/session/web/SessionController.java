package com.virtulab.platform.session.web;

import com.virtulab.platform.contracts.session.SessionResponse;
import com.virtulab.platform.contracts.session.StartSessionRequest;
import com.virtulab.platform.session.security.AuthContext;
import com.virtulab.platform.session.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public Mono<SessionResponse> start(
            @Valid @RequestBody StartSessionRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return sessionService.start(request, AuthContext.from(jwt));
    }

    @GetMapping("/{attemptId}")
    public Mono<SessionResponse> get(
            @PathVariable String attemptId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return sessionService.getByAttemptId(attemptId, AuthContext.from(jwt));
    }
}
