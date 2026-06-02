package com.virtulab.platform.session.security;

import com.virtulab.platform.contracts.security.JwtClaims;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

public record AuthContext(String userId, String tenantId, String orgId, java.util.List<String> roles) {
    public static AuthContext from(JwtClaims claims) {
        return new AuthContext(claims.userId(), claims.tenantId(), claims.orgId(), claims.roles());
    }

    public static AuthContext from(Jwt jwt) {
        String tenantId = jwt.getClaimAsString("tenantId");
        String orgId = jwt.getClaimAsString("orgId");

        // Keycloak default: realm_access.roles
        List<String> roles = List.of();
        Object realmAccess = jwt.getClaims().get("realm_access");
        if (realmAccess instanceof Map<?, ?> m) {
            Object rs = m.get("roles");
            if (rs instanceof List<?> list) {
                roles = list.stream().map(String::valueOf).toList();
            }
        }

        return new AuthContext(
                jwt.getSubject(),
                tenantId != null ? tenantId : "tenant-dev",
                orgId != null ? orgId : "org-dev",
                roles
        );
    }
}
