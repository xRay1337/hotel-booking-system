package org.service.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Hotel Service API v1
                .route("hotel-service-v1", r -> r
                        .path("/api/v1/hotels/**", "/api/v1/rooms/**")
                        .filters(f -> f
                                .addRequestHeader("X-API-Version", "v1")
                                .addResponseHeader("X-API-Version", "v1")
                        )
                        .uri("lb://HOTEL-SERVICE"))

                // Booking Service API v1
                .route("booking-service-v1", r -> r
                        .path("/api/v1/bookings/**", "/api/v1/users/**")
                        .filters(f -> f
                                .addRequestHeader("X-API-Version", "v1")
                                .addResponseHeader("X-API-Version", "v1")
                        )
                        .uri("lb://BOOKING-SERVICE"))

                // Public auth endpoints
                .route("booking-auth", r -> r
                        .path("/user/**")
                        .uri("lb://BOOKING-SERVICE"))
                .build();
    }
}