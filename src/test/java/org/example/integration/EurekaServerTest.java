package org.example.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class EurekaServerTest {

    @Test
    void eurekaServerContextLoads() {
        // Проверяем, что контекст Eureka Server загружается
        assertTrue(true, "Eureka Server context should load successfully");
    }
}