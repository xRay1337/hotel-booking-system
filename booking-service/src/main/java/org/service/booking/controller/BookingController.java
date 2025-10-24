package org.service.booking.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.booking.dto.BookingDTO;
import org.service.booking.dto.CreateBookingRequest;
import org.service.booking.entity.Booking;
import org.service.booking.mapper.BookingMapper;
import org.service.booking.service.BookingService;
import org.service.booking.service.UserService;
import org.service.booking.util.CorrelationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "API для управления бронированием")
public class BookingController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Создать бронирование", description = "Создание бронирования с выбором или автоподбором комнаты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Бронирование успешно создано"),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса"),
            @ApiResponse(responseCode = "404", description = "Комната или пользователь не найден"),
            @ApiResponse(responseCode = "409", description = "Комната недоступна для бронирования")
    })
    public ResponseEntity<BookingDTO> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        CorrelationContext.initCorrelationIdIfAbsent();
        log.info("Creating booking for user: {}, autoSelect: {}, roomId: {}",
                userDetails.getUsername(), request.getIsAutoSelect(), request.getRoomId());

        try {
            Booking booking;

            if (request.getIsAutoSelect()) {
                // Автоподбор комнаты
                booking = bookingService.createBookingWithAutoSelect(
                        userDetails.getUsername(),
                        request.getStartDate(),
                        request.getEndDate()
                );
            } else {
                // Бронирование конкретной комнаты
                if (request.getRoomId() == null) {
                    return ResponseEntity.badRequest().build();
                }

                booking = bookingService.createBookingWithRoomSelection(
                        userDetails.getUsername(),
                        request.getRoomId(),
                        request.getStartDate(),
                        request.getEndDate()
                );
            }

            log.info("Booking created successfully: {}", booking.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(bookingMapper.toDTO(booking));

        } catch (IllegalArgumentException e) {
            log.error("Invalid booking request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (org.service.booking.exception.RoomNotAvailableException e) {
            log.error("Room not available: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Failed to create booking: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Получить историю бронирований", description = "Получение истории всех бронирований текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "История бронирований получена успешно"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<List<BookingDTO>> getUserBookings(@AuthenticationPrincipal UserDetails userDetails) {
        CorrelationContext.initCorrelationIdIfAbsent();
        log.info("Getting booking history for user: {}", userDetails.getUsername());

        try {
            List<BookingDTO> bookings = bookingService.getUserBookings(userDetails.getUsername())
                    .stream()
                    .map(bookingMapper::toDTO)
                    .collect(Collectors.toList());

            log.info("Found {} bookings for user: {}", bookings.size(), userDetails.getUsername());
            return ResponseEntity.ok(bookings);

        } catch (Exception e) {
            log.error("Failed to get user bookings: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Получить бронирование по ID", description = "Получение информации о конкретном бронировании")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Бронирование найдено"),
            @ApiResponse(responseCode = "404", description = "Бронирование не найдено"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к бронированию")
    })
    public ResponseEntity<BookingDTO> getBooking(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        CorrelationContext.initCorrelationIdIfAbsent();
        log.info("Getting booking by ID: {} for user: {}", id, userDetails.getUsername());

        try {
            Booking booking = bookingService.getBookingByIdAndUser(id, userDetails.getUsername());
            return ResponseEntity.ok(bookingMapper.toDTO(booking));

        } catch (org.service.booking.exception.BookingNotFoundException e) {
            log.error("Booking not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            log.error("Access denied to booking: {} for user: {}", id, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Failed to get booking: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Отменить бронирование", description = "Отмена бронирования по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Бронирование успешно отменено"),
            @ApiResponse(responseCode = "404", description = "Бронирование не найдено"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к бронированию"),
            @ApiResponse(responseCode = "409", description = "Невозможно отменить бронирование")
    })
    public ResponseEntity<Void> cancelBooking(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        CorrelationContext.initCorrelationIdIfAbsent();
        log.info("Cancelling booking: {} for user: {}", id, userDetails.getUsername());

        try {
            bookingService.cancelBooking(id, userDetails.getUsername());
            log.info("Booking cancelled successfully: {}", id);
            return ResponseEntity.noContent().build();

        } catch (org.service.booking.exception.BookingNotFoundException e) {
            log.error("Booking not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            log.error("Access denied to cancel booking: {} for user: {}", id, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalStateException e) {
            log.error("Cannot cancel booking: {}, reason: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Failed to cancel booking: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}