package com.virtulab.platform.graphql.api;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class MeResolver {

    @QueryMapping
    public Me me(@AuthenticationPrincipal Jwt jwt) {
        String tenantId = jwt.getClaimAsString("tenantId");
        String orgId = jwt.getClaimAsString("orgId");

        List<String> roles = List.of();
        Object realmAccess = jwt.getClaims().get("realm_access");
        if (realmAccess instanceof Map<?, ?> m) {
            Object rs = m.get("roles");
            if (rs instanceof List<?> list) {
                roles = list.stream().map(String::valueOf).toList();
            }
        }

        return new Me(
                jwt.getSubject(),
                tenantId != null ? tenantId : "tenant-dev",
                orgId != null ? orgId : "org-dev",
                roles
        );
    }

    public record Me(String userId, String tenantId, String orgId, List<String> roles) {}
}

