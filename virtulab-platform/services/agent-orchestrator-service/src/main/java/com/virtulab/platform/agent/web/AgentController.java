package com.virtulab.platform.agent.web;

import com.virtulab.platform.contracts.ai.AgentRunRequest;
import com.virtulab.platform.contracts.ai.AgentRunResponse;
import com.virtulab.platform.agent.service.AgentOrchestratorService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    private final AgentOrchestratorService orchestratorService;

    public AgentController(AgentOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @PostMapping("/run")
    public Mono<AgentRunResponse> run(
            @Valid @RequestBody AgentRunRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return orchestratorService.run(request, jwt.getTokenValue());
    }
}
