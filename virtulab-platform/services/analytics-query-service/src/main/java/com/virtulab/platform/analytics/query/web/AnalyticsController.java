package com.virtulab.platform.analytics.query.web;

import com.virtulab.platform.analytics.query.security.AuthHelper;
import com.virtulab.platform.analytics.query.service.AnalyticsQueryService;
import com.virtulab.platform.contracts.analytics.AiVsProgressReport;
import com.virtulab.platform.contracts.analytics.ExperimentSummaryReport;
import com.virtulab.platform.contracts.analytics.OrgFunnelReport;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsQueryService queryService;

    public AnalyticsController(AnalyticsQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/org-funnel")
    public OrgFunnelReport orgFunnel(
            @RequestParam(defaultValue = "org-dev") String orgId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthHelper.assertOrgAccess(jwt, orgId);
        return queryService.orgFunnel(orgId);
    }

    @GetMapping("/experiment-summary")
    public ExperimentSummaryReport experimentSummary(
            @RequestParam String experimentId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return queryService.experimentSummary(experimentId);
    }

    @GetMapping("/ai-vs-progress")
    public AiVsProgressReport aiVsProgress(
            @RequestParam(defaultValue = "org-dev") String orgId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthHelper.assertOrgAccess(jwt, orgId);
        return queryService.aiVsProgress(orgId);
    }
}
