package org.example.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class BookingFlowIntegrationTest {

    @Container
    static GenericContainer<?> eurekaServer = new GenericContainer<>("springcloud/eureka")
            .withExposedPorts(8761)
            .withEnv("SERVER_PORT", "8761");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("eureka.client.service-url.defaultZone",
                () -> String.format("http://localhost:%d/eureka", eurekaServer.getFirstMappedPort()));
    }

    @Test
    void contextLoads() {
        assertTrue(true, "Context should load with Eureka server");
    }
}