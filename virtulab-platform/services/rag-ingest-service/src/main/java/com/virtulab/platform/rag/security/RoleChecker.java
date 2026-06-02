package com.virtulab.platform.rag.security;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

public final class RoleChecker {

    private RoleChecker() {}

    @SuppressWarnings("unchecked")
    public static void requireDeveloper(Jwt jwt) {
        List<String> roles = List.of();
        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof java.util.Map<?, ?> map) {
            Object r = map.get("roles");
            if (r instanceof List<?> list) {
                roles = list.stream().map(Object::toString).toList();
            }
        }
        if (!roles.contains("DEVELOPER") && !roles.contains("SUPER_ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "DEVELOPER or SUPER_ADMIN role required");
        }
    }
}
