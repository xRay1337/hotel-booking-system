package org.service.booking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

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
                        .anyRequest().permitAll() // РАЗРЕШАЕМ ВСЕ ЗАПРОСЫ
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(authz -> authz
//                        .requestMatchers(HttpMethod.POST,
//                                "/user/register", "/user/auth",
//                                "/booking-service/user/register", "/booking-service/user/auth",
//                                "**/user/register", "**/user/auth"
//                        ).permitAll()
//                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
//                .csrf(csrf -> csrf.disable());
//
//        return http.build();
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(authz -> authz
//                        // Actuator
//                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
//                        // Разрешаем доступ без авторизации
//                        .requestMatchers(HttpMethod.POST,"**/user/register", "**/user/auth").permitAll()
//
//                        // Разрешаем доступ к Swagger без авторизации
//                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
//
//                        // Бронирования - для USER и ADMIN
//                        .requestMatchers(HttpMethod.GET, "/bookings/**", "/api/bookings/**").hasAnyRole("USER", "ADMIN")
//                        .requestMatchers(HttpMethod.POST, "/bookings/**", "/api/bookings/**").hasAnyRole("USER", "ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/bookings/**", "/api/bookings/**").hasAnyRole("USER", "ADMIN")
//
//                        // Админские эндпоинты пользователей
//                        .requestMatchers(HttpMethod.GET, "/user", "/api/user").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.GET, "/user/*", "/api/user/*").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.PUT, "/user/*", "/api/user/*").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/user/*", "/api/user/*").hasRole("ADMIN")
//
//                        // Все остальные запросы требуют аутентификации
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