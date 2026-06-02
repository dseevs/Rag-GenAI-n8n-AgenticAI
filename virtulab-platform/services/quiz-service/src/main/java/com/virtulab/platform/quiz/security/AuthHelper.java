package com.virtulab.platform.quiz.security;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

public final class AuthHelper {

    private AuthHelper() {}

    public static String userId(Jwt jwt) {
        String sub = jwt.getSubject();
        return sub != null ? sub : jwt.getClaimAsString("preferred_username");
    }

    public static String orgId(Jwt jwt) {
        String org = jwt.getClaimAsString("orgId");
        return org != null ? org : "org-dev";
    }

    public static List<String> roles(Jwt jwt) {
        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof java.util.Map<?, ?> map && map.get("roles") instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    public static void assertSuperAdmin(Jwt jwt) {
        if (!roles(jwt).contains("SUPER_ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "SUPER_ADMIN role required");
        }
    }
}
