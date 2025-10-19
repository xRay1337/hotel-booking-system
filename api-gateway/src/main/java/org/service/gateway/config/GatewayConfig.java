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
                // Hotel Service routes
                .route("hotel-service", r -> r
                        .path("/api/hotels/**", "/api/rooms/**", "/api/hotel/debug/**")
                        .uri("lb://HOTEL-SERVICE"))
                // Booking Service API routes
                .route("booking-service-api", r -> r
                        .path("/api/user/**", "/api/bookings/**", "/api/debug/**")
                        .filters(f -> f.rewritePath("/api/(?<segment>.*)", "/${segment}"))
                        .uri("lb://BOOKING-SERVICE"))
                // Booking Service legacy routes
                .route("booking-service", r -> r
                        .path("/bookings/**")
                        .uri("lb://BOOKING-SERVICE"))
                // Public auth endpoints
                .route("booking-auth", r -> r
                        .path("/user/**")
                        .uri("lb://BOOKING-SERVICE"))
                .build();
    }
}