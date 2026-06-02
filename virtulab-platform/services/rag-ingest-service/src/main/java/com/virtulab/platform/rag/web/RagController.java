package com.virtulab.platform.rag.web;

import com.virtulab.platform.contracts.ai.RagJobStatusResponse;
import com.virtulab.platform.contracts.ai.RagReindexResponse;
import com.virtulab.platform.contracts.ai.RagStatsResponse;
import com.virtulab.platform.rag.security.RoleChecker;
import com.virtulab.platform.rag.service.ReindexService;
import com.virtulab.platform.rag.temporal.TemporalReindexFacade;
import java.util.Optional;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/rag")
public class RagController {

    private final ReindexService reindexService;
    private final Optional<TemporalReindexFacade> temporal;

    public RagController(ReindexService reindexService, Optional<TemporalReindexFacade> temporal) {
        this.reindexService = reindexService;
        this.temporal = temporal;
    }

    @PostMapping("/reindex")
    public Mono<RagReindexResponse> reindex(@AuthenticationPrincipal Jwt jwt) {
        RoleChecker.requireDeveloper(jwt);
        return temporal.map(TemporalReindexFacade::start).orElseGet(reindexService::startReindex);
    }

    @GetMapping("/jobs/{jobId}")
    public Mono<RagJobStatusResponse> job(@PathVariable("jobId") String jobId, @AuthenticationPrincipal Jwt jwt) {
        RoleChecker.requireDeveloper(jwt);
        return temporal.map(t -> t.status(jobId)).orElseGet(() -> reindexService.jobStatus(jobId));
    }

    @GetMapping("/stats")
    public Mono<RagStatsResponse> stats(@AuthenticationPrincipal Jwt jwt) {
        RoleChecker.requireDeveloper(jwt);
        return reindexService.stats();
    }
}
