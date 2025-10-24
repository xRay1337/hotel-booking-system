package org.example.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class GatewayRoutingTest {

    @Autowired(required = false)
    private RouteLocator routeLocator;

    @Test
    void gatewayRoutesConfigured() {
        // Проверяем, что RouteLocator создан (значит Gateway настроен)
        assertNotNull(routeLocator, "RouteLocator should be configured for API Gateway");
    }

    @Test
    void contextLoads() {
        // Basic test to verify Spring context loads
        assertNotNull(routeLocator);
    }
}