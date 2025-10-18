package org.service.hotel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec secretKey = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtConverter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // Actuator health
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        // Публичные эндпоинты для проверки доступности
                        .requestMatchers("/rooms/*/confirm-room-availability").permitAll()
                        .requestMatchers("/rooms/*/release-room").permitAll()
                        // ПРОСТАЯ КОНФИГУРАЦИЯ - разрешаем все GET, ограничиваем POST
                        .requestMatchers(HttpMethod.GET, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(authz -> authz
//                        // Actuator health
//                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
//                        // Публичные эндпоинты для проверки доступности
//                        .requestMatchers("/rooms/*/confirm-room-availability").permitAll()
//                        .requestMatchers("/rooms/*/release-room").permitAll()
//                        // Публичные эндпоинты для пользователей
//                        .requestMatchers(HttpMethod.GET, "/hotels", "/hotels/{id}", "/api/hotels", "/api/hotels/{id}").hasAnyRole("USER", "ADMIN")
//                        .requestMatchers(HttpMethod.GET, "/rooms", "/rooms/available", "/rooms/recommend", "/rooms/{id}",
//                                "/api/rooms", "/api/rooms/available", "/api/rooms/recommend", "/api/rooms/{id}").hasAnyRole("USER", "ADMIN")
//                        // Административные эндпоинты - ИСПРАВЛЯЕМ ПУТИ
//                        .requestMatchers(HttpMethod.POST, "/hotels", "/api/hotels", "/hotels/**", "/api/hotels/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.PUT, "/hotels/**", "/api/hotels/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/hotels/**", "/api/hotels/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.POST, "/rooms", "/api/rooms", "/rooms/**", "/api/rooms/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.PUT, "/rooms/**", "/api/rooms/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/rooms/**", "/api/rooms/**").hasRole("ADMIN")
//                        .anyRequest().authenticated()
//                )
//                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(jwt -> jwt
//                                .decoder(jwtDecoder())
//                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
//                        )
//                )
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .csrf(csrf -> csrf.disable());
//
//        return http.build();
//    }
}