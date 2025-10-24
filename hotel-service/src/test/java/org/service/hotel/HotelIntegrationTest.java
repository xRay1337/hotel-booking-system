package org.service.hotel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class HotelIntegrationTest {

    @Test
    void contextLoads() {
        // Basic test to verify Spring context loads successfully
        assertTrue(true, "Spring context should load successfully");
    }
}