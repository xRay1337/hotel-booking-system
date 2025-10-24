package org.service.booking.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.booking.dto.RoomDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotelServiceClient {

    private final WebClient webClient;

    // Теперь обращаемся через gateway
    @Value("${gateway.url:http://localhost:8080}")
    private String gatewayUrl;

    public List<RoomDTO> findAvailableRooms(LocalDate startDate, LocalDate endDate) {
        try {
            // Через gateway: /api/hotels/rooms/available
            String url = UriComponentsBuilder.fromHttpUrl(gatewayUrl + "/api/hotels/rooms/available")
                    .queryParam("startDate", startDate)
                    .queryParam("endDate", endDate)
                    .toUriString();

            log.debug("Calling hotel service via gateway: {}", url);

            RoomDTO[] rooms = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(RoomDTO[].class)
                    .block();

            List<RoomDTO> result = rooms != null ? Arrays.asList(rooms) : Collections.emptyList();
            log.info("Found {} available rooms for dates {} to {}", result.size(), startDate, endDate);

            return result;

        } catch (WebClientResponseException.NotFound e) {
            log.info("No available rooms found for dates {} to {}", startDate, endDate);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error finding available rooms for dates {} to {}: {}", startDate, endDate, e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean checkRoomAvailability(Long roomId, LocalDate startDate, LocalDate endDate) {
        try {
            // Через gateway: /api/hotels/rooms/{roomId}/availability
            String url = UriComponentsBuilder.fromHttpUrl(gatewayUrl + "/api/hotels/rooms/{roomId}/availability")
                    .queryParam("startDate", startDate)
                    .queryParam("endDate", endDate)
                    .buildAndExpand(roomId)
                    .toUriString();

            log.debug("Checking room availability via gateway: {}", url);

            Boolean isAvailable = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            boolean available = Boolean.TRUE.equals(isAvailable);
            log.info("Room {} availability for dates {} to {}: {}", roomId, startDate, endDate, available);

            return available;

        } catch (WebClientResponseException.NotFound e) {
            log.warn("Room {} not found", roomId);
            return false;
        } catch (Exception e) {
            log.error("Error checking room availability for room {}: {}", roomId, e.getMessage());
            return false;
        }
    }

    public boolean lockRoom(Long roomId, LocalDate startDate, LocalDate endDate) {
        try {
            // Через gateway: /api/hotels/rooms/{roomId}/lock
            String url = UriComponentsBuilder.fromHttpUrl(gatewayUrl + "/api/hotels/rooms/{roomId}/lock")
                    .queryParam("startDate", startDate)
                    .queryParam("endDate", endDate)
                    .buildAndExpand(roomId)
                    .toUriString();

            log.debug("Locking room via gateway: {}", url);

            Boolean locked = webClient.post()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            boolean success = Boolean.TRUE.equals(locked);
            log.info("Room {} lock result for dates {} to {}: {}", roomId, startDate, endDate, success);

            return success;

        } catch (WebClientResponseException.Conflict e) {
            log.warn("Room {} is already locked for dates {} to {}", roomId, startDate, endDate);
            return false;
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Room {} not found for locking", roomId);
            return false;
        } catch (Exception e) {
            log.error("Error locking room {}: {}", roomId, e.getMessage());
            return false;
        }
    }

    public void releaseRoom(Long roomId, LocalDate startDate, LocalDate endDate) {
        try {
            // Через gateway: /api/hotels/rooms/{roomId}/release
            String url = UriComponentsBuilder.fromHttpUrl(gatewayUrl + "/api/hotels/rooms/{roomId}/release")
                    .queryParam("startDate", startDate)
                    .queryParam("endDate", endDate)
                    .buildAndExpand(roomId)
                    .toUriString();

            log.debug("Releasing room via gateway: {}", url);

            webClient.post()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Room {} released successfully for dates {} to {}", roomId, startDate, endDate);

        } catch (WebClientResponseException.NotFound e) {
            log.warn("Room {} not found for release", roomId);
        } catch (Exception e) {
            log.error("Error releasing room {}: {}", roomId, e.getMessage());
        }
    }
}