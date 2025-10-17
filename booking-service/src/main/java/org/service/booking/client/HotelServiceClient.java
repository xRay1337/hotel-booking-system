package org.service.booking.client;

import org.service.booking.dto.RoomAvailabilityRequest;
import org.service.booking.dto.RoomDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class HotelServiceClient {

    private final WebClient webClient;

    public HotelServiceClient(@Value("${client.hotel-service.url:http://localhost:8081}") String hotelServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(hotelServiceUrl)
                .build();
    }

    public Mono<RoomDTO> confirmRoomAvailability(Long roomId, RoomAvailabilityRequest request) {
        return webClient.post()
                .uri("/api/rooms/{id}/confirm-availability", roomId)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatus.CONFLICT::equals, response ->
                        Mono.error(new RuntimeException("Room is not available")))
                .bodyToMono(RoomDTO.class)
                .onErrorMap(WebClientResponseException.class, ex ->
                        new RuntimeException("Hotel Service error: " + ex.getResponseBodyAsString()));
    }

    public Mono<Void> releaseRoom(Long roomId) {
        return webClient.post()
                .uri("/api/rooms/{id}/release", roomId)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorMap(WebClientResponseException.class, ex ->
                        new RuntimeException("Hotel Service error: " + ex.getResponseBodyAsString()));
    }

    public Mono<RoomDTO> getRoomById(Long roomId) {
        return webClient.get()
                .uri("/api/rooms/{id}", roomId)
                .retrieve()
                .bodyToMono(RoomDTO.class)
                .onErrorMap(WebClientResponseException.class, ex ->
                        new RuntimeException("Hotel Service error: " + ex.getResponseBodyAsString()));
    }
}