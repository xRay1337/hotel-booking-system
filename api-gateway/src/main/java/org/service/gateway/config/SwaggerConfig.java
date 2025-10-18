package org.service.gateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🏨 Система бронирования отелей - Полная документация API")
                        .version("1.0")
                        .description("""
                            ## Микросервисная система бронирования отелей
                            
                            ### 🎯 Архитектура системы:
                            - **API Gateway** - единая точка входа, маршрутизация, аутентификация
                            - **Booking Service** - управление бронированиями и пользователями  
                            - **Hotel Service** - управление отелями и номерами
                            - **Eureka Server** - сервис обнаружения микросервисов
                            
                            ### 🔐 Быстрое получение JWT токенов:
                            
                            #### Получить USER токен через PowerShell:
                            ```powershell
                            (irm http://localhost:8080/booking-service/user/register -Method POST -Headers @{"Content-Type"="application/json"} -Body '{"username":"user","password":"qwerty","email":"user@email.ru","role":"USER"}').token
                            ```
                            
                            #### Получить ADMIN токен через PowerShell:
                            ```powershell
                            (irm http://localhost:8080/booking-service/user/register -Method POST -Headers @{"Content-Type"="application/json"} -Body '{"username":"admin","password":"qwerty","email":"admin@email.ru","role":"ADMIN"}').token
                            ```
                            
                            ### 📡 Выберите сервис для просмотра документации:
                            Используйте выпадающий список выше для переключения между документацией разных сервисов.
                            
                            ### 🚀 Процесс тестирования:
                            1. **Запустите PowerShell скрипт выше** чтобы получить JWT токен
                            2. **Нажмите "Authorize"** вверху страницы  
                            3. **Введите токен** в формате: `Bearer ваш_токен`
                            4. **Тестируйте endpoints** с авторизацией
                            
                            ### 🔄 Рекомендуемая последовательность тестирования:
                            1. Получите USER токен через скрипт
                            2. Протестируйте бронирования через Booking Service
                            3. Получите ADMIN токен через скрипт  
                            4. Протестируйте административные функции
                            5. Протестируйте управление отелями через Hotel Service
                            
                            ### 🛠️ Доступные сервисы:
                            - **Booking Service** - регистрация, аутентификация, бронирования
                            - **Hotel Service** - отели, номера, рекомендации
                            - **API Gateway** - общая информация и агрегация
                            """))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Gateway Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .name("JWT")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT токен авторизации.")));
    }
}