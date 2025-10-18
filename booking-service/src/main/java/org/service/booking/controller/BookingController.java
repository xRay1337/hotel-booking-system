package org.service.booking.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.booking.dto.BookingDTO;
import org.service.booking.dto.CreateBookingRequest;
import org.service.booking.entity.Booking;
import org.service.booking.mapper.BookingMapper;
import org.service.booking.service.BookingService;
import org.service.booking.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "API для управления бронированием")
public class BookingController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingDTO> createBooking(@RequestBody CreateBookingRequest request) {
        log.info("Creating booking for user ID: {}, room ID: {}", request.getUserId(), request.getRoomId());

        try {
            // Ручное создание Booking из CreateBookingRequest
            Booking booking = Booking.builder()
                    .user(userService.getUserById(request.getUserId())) // Получаем User entity
                    .roomId(request.getRoomId())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .status(Booking.BookingStatus.PENDING)
                    .correlationId(UUID.randomUUID().toString())
                    .createdAt(LocalDateTime.now())
                    .build();

            Booking createdBooking = bookingService.createBookingWithSaga(booking);

            log.info("Booking created successfully: {}", createdBooking.getId());
            return ResponseEntity.ok(bookingMapper.toDTO(createdBooking));

        } catch (Exception e) {
            log.error("Failed to create booking: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Остальные методы без изменений...
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<BookingDTO>> getUserBookings() {
        List<BookingDTO> bookings = bookingService.getAllBookings().stream()
                .map(bookingMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingDTO> getBooking(@PathVariable Long id) {
        try {
            Booking booking = bookingService.getBookingById(id);
            return ResponseEntity.ok(bookingMapper.toDTO(booking));
        } catch (Exception e) {
            log.error("Booking not found: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        try {
            bookingService.cancelBooking(id);
            log.info("Booking cancelled: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to cancel booking: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingDTO> confirmBooking(@PathVariable Long id) {
        try {
            Booking confirmedBooking = bookingService.confirmBooking(id);
            return ResponseEntity.ok(bookingMapper.toDTO(confirmedBooking));
        } catch (Exception e) {
            log.error("Failed to confirm booking: {}", id);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        List<BookingDTO> bookings = bookingService.getAllBookings().stream()
                .map(bookingMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }
}