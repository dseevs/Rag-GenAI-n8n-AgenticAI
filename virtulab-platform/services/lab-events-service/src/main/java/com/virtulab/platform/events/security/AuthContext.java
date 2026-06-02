package com.virtulab.platform.events.security;

import com.virtulab.platform.contracts.security.JwtClaims;
import org.springframework.security.oauth2.jwt.Jwt;

public record AuthContext(String userId, String tenantId, String orgId) {
    public static AuthContext from(JwtClaims claims) {
        return new AuthContext(claims.userId(), claims.tenantId(), claims.orgId());
    }

    public static AuthContext from(Jwt jwt) {
        String tenantId = jwt.getClaimAsString("tenantId");
        String orgId = jwt.getClaimAsString("orgId");
        return new AuthContext(
                jwt.getSubject(),
                tenantId != null ? tenantId : "tenant-dev",
                orgId != null ? orgId : "org-dev"
        );
    }
}
