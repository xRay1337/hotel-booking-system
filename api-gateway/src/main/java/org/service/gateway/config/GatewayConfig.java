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
                // Hotel Service routes - пути как в HotelController
                .route("hotel-service", r -> r
                        .path("/hotels/**")
                        .uri("lb://HOTEL-SERVICE"))

                // Booking Service routes - пути как в BookingController
                .route("booking-service", r -> r
                        .path("/bookings/**")
                        .uri("lb://BOOKING-SERVICE"))

                // Public auth endpoints - пути как в UserController
                .route("booking-auth", r -> r
                        .path("/user/**")
                        .uri("lb://BOOKING-SERVICE"))
                .build();
    }
}