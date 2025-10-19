package org.service.hotel.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class HotelServiceClient {

    private final WebClient webClient;

    @Value("${booking.service.url:http://localhost:8082}")
    private String bookingServiceUrl;

    public boolean hasConflictingBookings(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        try {
            String url = bookingServiceUrl + "/api/bookings/rooms/" + roomId + "/conflicts" +
                    "?checkIn=" + checkIn + "&checkOut=" + checkOut;

            Boolean hasConflicts = webClient.get()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            Mono.error(new RuntimeException("Failed to check booking conflicts")))
                    .bodyToMono(Boolean.class)
                    .block();

            return Boolean.TRUE.equals(hasConflicts);
        } catch (Exception e) {
            throw new RuntimeException("Error checking booking conflicts for room: " + roomId, e);
        }
    }

    public Integer getActiveBookingsCount(Long roomId) {
        try {
            return webClient.get()
                    .uri(bookingServiceUrl + "/api/bookings/rooms/{roomId}/active-count", roomId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            Mono.error(new RuntimeException("Failed to get active bookings count")))
                    .bodyToMono(Integer.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Error getting active bookings count for room: " + roomId, e);
        }
    }
}