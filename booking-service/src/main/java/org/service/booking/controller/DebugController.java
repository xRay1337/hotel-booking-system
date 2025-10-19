package org.service.booking.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/booking/debug")
public class DebugController {

    @GetMapping("/jwt")
    public Map<String, Object> debugJwt(@AuthenticationPrincipal Jwt jwt) {
        log.info("JWT Debug endpoint called");

        Map<String, Object> debugInfo = new HashMap<>();
        if (jwt != null) {
            debugInfo.put("subject", jwt.getSubject());
            debugInfo.put("issuedAt", jwt.getIssuedAt());
            debugInfo.put("expiresAt", jwt.getExpiresAt());
            debugInfo.put("claims", jwt.getClaims());
            debugInfo.put("headers", jwt.getHeaders());

            // Проверяем roles claim
            Object roles = jwt.getClaim("roles");
            debugInfo.put("rolesClaim", roles);
            debugInfo.put("rolesClaimType", roles != null ? roles.getClass().getSimpleName() : "null");

            log.info("JWT Debug: {}", debugInfo);
        } else {
            debugInfo.put("error", "JWT is null");
            log.warn("JWT is null in debug endpoint");
        }

        return debugInfo;
    }

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This is public endpoint - no authentication required";
    }

    @GetMapping("/protected")
    public String protectedEndpoint(@AuthenticationPrincipal Jwt jwt) {
        return "This is protected endpoint - user: " + (jwt != null ? jwt.getSubject() : "anonymous");
    }
}