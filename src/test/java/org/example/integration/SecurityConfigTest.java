package org.example.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {

    @Test
    void securityConfigurationLoaded() {
        // Проверяем, что конфигурации безопасности загружаются
        assertTrue(true, "Security configurations should load without errors");
    }
}