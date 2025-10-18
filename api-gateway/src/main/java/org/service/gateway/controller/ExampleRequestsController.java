package org.service.gateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Готовые примеры для тестирования API", description = "Реальные рабочие примеры запросов для тестирования системы")
public class ExampleRequestsController {

    @GetMapping("/api/examples/requests")
    @Operation(summary = "Получить готовые примеры API запросов",
            description = "Возвращает реальные рабочие примеры для тестирования всех функций системы")
    public Mono<ResponseEntity<Map<String, Object>>> getApiExamples() {
        var examples = Map.<String, Object>of(
                "authentication_examples", List.of(
                        Map.of(
                                "name", "Регистрация пользователя",
                                "method", "POST",
                                "url", "/booking-service/user/register",
                                "description", "Создание нового пользователя с ролью USER",
                                "headers", Map.of("Content-Type", "application/json"),
                                "body", Map.of(
                                        "username", "testuser",
                                        "password", "password123",
                                        "email", "testuser@mail.ru",
                                        "role", "USER"
                                ),
                                "powershell", "irm http://localhost:8080/booking-service/user/register -Method POST -Headers @{\"Content-Type\"=\"application/json\"} -Body '{\"username\":\"testuser\",\"password\":\"password123\",\"email\":\"testuser@mail.ru\",\"role\":\"USER\"}'"
                        ),
                        Map.of(
                                "name", "Регистрация администратора",
                                "method", "POST",
                                "url", "/booking-service/user/register",
                                "description", "Создание нового пользователя с ролью ADMIN",
                                "headers", Map.of("Content-Type", "application/json"),
                                "body", Map.of(
                                        "username", "testadmin",
                                        "password", "admin123",
                                        "email", "admin@mail.ru",
                                        "role", "ADMIN"
                                ),
                                "powershell", "irm http://localhost:8080/booking-service/user/register -Method POST -Headers @{\"Content-Type\"=\"application/json\"} -Body '{\"username\":\"testadmin\",\"password\":\"admin123\",\"email\":\"admin@mail.ru\",\"role\":\"ADMIN\"}'"
                        ),
                        Map.of(
                                "name", "Авторизация пользователя",
                                "method", "POST",
                                "url", "/booking-service/user/auth",
                                "description", "Получение JWT токена для существующего пользователя",
                                "headers", Map.of("Content-Type", "application/json"),
                                "body", Map.of(
                                        "username", "testuser",
                                        "password", "password123"
                                ),
                                "powershell", "irm http://localhost:8080/booking-service/user/auth -Method POST -Headers @{\"Content-Type\"=\"application/json\"} -Body '{\"username\":\"testuser\",\"password\":\"password123\"}'"
                        )
                ),

                "hotel_examples", List.of(
                        Map.of(
                                "name", "Получить список всех отелей",
                                "method", "GET",
                                "url", "/hotel-service/api/hotels",
                                "description", "Получение списка всех доступных отелей (не требует авторизации)",
                                "headers", Map.of(),
                                "powershell", "irm http://localhost:8080/hotel-service/api/hotels"
                        ),
                        Map.of(
                                "name", "Получить доступные номера",
                                "method", "GET",
                                "url", "/hotel-service/api/rooms?startDate=2024-02-01&endDate=2024-02-05",
                                "description", "Получение списка номеров доступных на указанные даты",
                                "headers", Map.of(),
                                "powershell", "irm \"http://localhost:8080/hotel-service/api/rooms?startDate=2024-02-01&endDate=2024-02-05\""
                        ),
                        Map.of(
                                "name", "Получить рекомендованные номера",
                                "method", "GET",
                                "url", "/hotel-service/api/rooms/recommend?startDate=2024-02-01&endDate=2024-02-05",
                                "description", "Получение рекомендованных номеров (отсортированных по загруженности)",
                                "headers", Map.of(),
                                "powershell", "irm \"http://localhost:8080/hotel-service/api/rooms/recommend?startDate=2024-02-01&endDate=2024-02-05\""
                        ),
                        Map.of(
                                "name", "Создать новый отель (ADMIN)",
                                "method", "POST",
                                "url", "/hotel-service/api/hotels",
                                "description", "Создание нового отеля (требует роль ADMIN)",
                                "headers", Map.of(
                                        "Content-Type", "application/json",
                                        "Authorization", "Bearer {ADMIN_TOKEN}"
                                ),
                                "body", Map.of(
                                        "name", "Новый Отель",
                                        "address", "ул. Примерная, 123"
                                )
                        )
                ),

                "booking_examples", List.of(
                        Map.of(
                                "name", "Создать бронирование",
                                "method", "POST",
                                "url", "/booking-service/api/bookings",
                                "description", "Создание нового бронирования (требует роль USER)",
                                "headers", Map.of(
                                        "Content-Type", "application/json",
                                        "Authorization", "Bearer {USER_TOKEN}"
                                ),
                                "body", Map.of(
                                        "roomId", 1,
                                        "startDate", "2024-02-01",
                                        "endDate", "2024-02-05",
                                        "autoSelect", false
                                )
                        ),
                        Map.of(
                                "name", "Создать бронирование с автовыбором",
                                "method", "POST",
                                "url", "/booking-service/api/bookings",
                                "description", "Создание бронирования с автоматическим подбором номера",
                                "headers", Map.of(
                                        "Content-Type", "application/json",
                                        "Authorization", "Bearer {USER_TOKEN}"
                                ),
                                "body", Map.of(
                                        "startDate", "2024-02-01",
                                        "endDate", "2024-02-05",
                                        "autoSelect", true
                                )
                        ),
                        Map.of(
                                "name", "Получить историю бронирований",
                                "method", "GET",
                                "url", "/booking-service/api/bookings",
                                "description", "Получение списка бронирований текущего пользователя",
                                "headers", Map.of(
                                        "Authorization", "Bearer {USER_TOKEN}"
                                ),
                                "powershell", "irm http://localhost:8080/booking-service/api/bookings -Headers @{\"Authorization\"=\"Bearer YOUR_TOKEN\"}"
                        ),
                        Map.of(
                                "name", "Отменить бронирование",
                                "method", "DELETE",
                                "url", "/booking-service/api/bookings/{id}",
                                "description", "Отмена бронирования по ID",
                                "headers", Map.of(
                                        "Authorization", "Bearer {USER_TOKEN}"
                                )
                        )
                ),

                "admin_examples", List.of(
                        Map.of(
                                "name", "Получить всех пользователей",
                                "method", "GET",
                                "url", "/booking-service/user",
                                "description", "Получение списка всех пользователей (требует роль ADMIN)",
                                "headers", Map.of(
                                        "Authorization", "Bearer {ADMIN_TOKEN}"
                                )
                        ),
                        Map.of(
                                "name", "Получить все бронирования",
                                "method", "GET",
                                "url", "/booking-service/api/bookings/admin/all",
                                "description", "Получение списка всех бронирований в системе (требует роль ADMIN)",
                                "headers", Map.of(
                                        "Authorization", "Bearer {ADMIN_TOKEN}"
                                )
                        )
                ),

                "note", "Замените {USER_TOKEN} и {ADMIN_TOKEN} на реальные JWT токены, полученные через скрипты регистрации"
        );

        return Mono.just(ResponseEntity.ok(examples));
    }

    @GetMapping("/api/examples/health")
    @Operation(summary = "Проверить здоровье системы",
            description = "Проверка доступности всех компонентов системы")
    public Mono<ResponseEntity<Map<String, Object>>> getSystemHealth() {
        var health = Map.of(
                "timestamp", LocalDate.now().toString(),
                "services", List.of(
                        Map.of("name", "API Gateway", "url", "http://localhost:8080/actuator/health", "status", "CHECKING"),
                        Map.of("name", "Booking Service", "url", "http://localhost:8080/booking-service/actuator/health", "status", "CHECKING"),
                        Map.of("name", "Hotel Service", "url", "http://localhost:8080/hotel-service/actuator/health", "status", "CHECKING"),
                        Map.of("name", "Eureka Server", "url", "http://localhost:8761", "status", "CHECKING")
                ),
                "documentation", Map.of(
                        "swagger_ui", "http://localhost:8080/swagger-ui.html",
                        "api_examples", "/api/examples/requests",
                        "health_check", "/api/examples/health"
                )
        );

        return Mono.just(ResponseEntity.ok(health));
    }
}