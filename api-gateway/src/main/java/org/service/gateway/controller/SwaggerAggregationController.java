package org.service.gateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "Документация API", description = "Ссылки на всю документацию системы")
public class SwaggerAggregationController {

    @GetMapping("/api/documentation")
    @Operation(
            summary = "Получить все ссылки на документацию",
            description = "Возвращает ссылки на Swagger UI всех сервисов системы"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешный ответ со всеми ссылками на документацию",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                    {
                      "gateway_documentation": {
                        "url": "/swagger-ui.html",
                        "description": "API Gateway - единая точка входа"
                      },
                      "services": {
                        "booking_service": {
                          "name": "Booking Service",
                          "api_docs": "/booking-service/v3/api-docs",
                          "description": "Управление бронированиями и пользователями"
                        },
                        "hotel_service": {
                          "name": " Hotel Service",
                          "api_docs": "/hotel-service/v3/api-docs",
                          "description": "Управление отелями и номерами"
                        }
                      },
                      "quick_links": {
                        "api_examples": "/api/examples/requests",
                        "system_health": "/api/examples/health"
                      },
                      "note": "Используйте Swagger UI Gateway для тестирования API через единую точку входа"
                    }
                    """
                    )
            )
    )
    public ResponseEntity<Map<String, Object>> getAllDocumentation() {
        Map<String, Object> response = new HashMap<>();

        response.put("gateway_documentation", Map.of(
                "url", "/swagger-ui.html",
                "description", "API Gateway - единая точка входа"
        ));

        response.put("services", Map.of(
                "booking_service", Map.of(
                        "name", "Booking Service",
                        "api_docs", "/booking-service/v3/api-docs",
                        "description", "Управление бронированиями и пользователями"
                ),
                "hotel_service", Map.of(
                        "name", "Hotel Service",
                        "api_docs", "/hotel-service/v3/api-docs",
                        "description", "Управление отелями и номерами"
                )
        ));

        response.put("quick_links", Map.of(
                "api_examples", "/api/examples/requests",
                "system_health", "/api/examples/health"
        ));

        response.put("note", "Используйте Swagger UI Gateway для тестирования API через единую точку входа");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/swagger-urls")
    @Operation(
            summary = "URLs для Swagger агрегации",
            description = "Возвращает URLs для настройки Swagger UI агрегации"
    )
    @ApiResponse(
            responseCode = "200",
            description = "URLs для настройки Swagger UI",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                    {
                      "urls": [
                        {
                          "name": "API Gateway",
                          "url": "/v3/api-docs",
                          "primary": true
                        },
                        {
                          "name": "Booking Service", 
                          "url": "/booking-service/v3/api-docs"
                        },
                        {
                          "name": "Hotel Service",
                          "url": "/hotel-service/v3/api-docs"
                        }
                      ]
                    }
                    """
                    )
            )
    )
    public ResponseEntity<Map<String, Object>> getSwaggerUrls() {
        Map<String, Object> response = new HashMap<>();

        response.put("urls", new Object[]{
                Map.of("name", "API Gateway", "url", "/v3/api-docs", "primary", true),
                Map.of("name", "Booking Service", "url", "/booking-service/v3/api-docs"),
                Map.of("name", "Hotel Service", "url", "/hotel-service/v3/api-docs")
        });

        return ResponseEntity.ok(response);
    }
}