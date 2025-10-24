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
                .route("hotel-service", r -> r
                        .path("/api/hotels/**", "/api/rooms/**")
                        .uri("lb://HOTEL-SERVICE"))
                .route("booking-service", r -> r
                        .path("/api/user/**", "/api/bookings/**")
                        .uri("lb://BOOKING-SERVICE"))
                .build();
    }
}