package com.virtulab.platform.ml.security;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

public final class AuthHelper {

    private AuthHelper() {}

    public static List<String> roles(Jwt jwt) {
        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof java.util.Map<?, ?> map && map.get("roles") instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    public static void assertDeveloperOrAbove(Jwt jwt) {
        List<String> roles = roles(jwt);
        if (roles.contains("DEVELOPER") || roles.contains("SUPER_ADMIN")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "DEVELOPER role required");
    }

    public static String orgId(Jwt jwt) {
        String org = jwt.getClaimAsString("orgId");
        return org != null ? org : "org-dev";
    }
}
