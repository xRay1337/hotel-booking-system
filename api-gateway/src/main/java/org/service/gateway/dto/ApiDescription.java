package org.service.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Описание API endpoint")
public class ApiDescription {

    @Schema(description = "HTTP метод", example = "GET")
    private String method;

    @Schema(description = "URL path", example = "/api/bookings")
    private String path;

    @Schema(description = "Описание endpoint", example = "Получить историю бронирований пользователя")
    private String description;

    @Schema(description = "Необходимые роли", example = "USER")
    private String requiredRole;
}
