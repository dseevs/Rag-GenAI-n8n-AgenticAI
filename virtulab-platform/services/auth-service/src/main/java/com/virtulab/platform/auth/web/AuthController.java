package com.virtulab.platform.auth.web;

import com.virtulab.platform.contracts.auth.DevTokenRequest;
import com.virtulab.platform.contracts.auth.DevTokenResponse;
import com.virtulab.platform.contracts.auth.TokenValidationRequest;
import com.virtulab.platform.contracts.auth.TokenValidationResponse;
import com.virtulab.platform.auth.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final TokenService tokenService;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/validate")
    public Mono<TokenValidationResponse> validate(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody(required = false) TokenValidationRequest body
    ) {
        String token = authorization;
        if ((token == null || token.isBlank()) && body != null) {
            token = body.token();
        }
        return tokenService.validate(token);
    }

    /** Phase 1: issue dev JWT for Postman (disable in production). */
    @PostMapping("/token")
    public DevTokenResponse issueToken(@Valid @RequestBody DevTokenRequest request) {
        return tokenService.issueDevToken(request);
    }
}
