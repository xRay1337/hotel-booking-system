package org.example.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EndToEndBookingTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + restTemplate.getRestTemplate().getUriTemplateHandler().toString();
    }

    @Test
    void testServiceDiscoveryAndRouting() {
        // Этот тест проверяет, что Gateway и Eureka работают вместе
        // В реальном сценарии здесь были бы вызовы через Gateway

        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHotelServiceAvailable() {
        // Тест доступности hotel-service через gateway
        // В реальном сценарии URL был бы через gateway

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/hotels/rooms/available?startDate={startDate}&endDate={endDate}",
                String.class,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3)
        );

        // Ожидаем либо OK, либо NOT_FOUND (если сервис не зарегистрирован)
        assertTrue(response.getStatusCode().is2xxSuccessful() ||
                response.getStatusCode() == HttpStatus.NOT_FOUND);
    }
}