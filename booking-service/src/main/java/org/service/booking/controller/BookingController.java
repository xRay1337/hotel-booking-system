package org.service.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.booking.dto.BookingDTO;
import org.service.booking.dto.CreateBookingRequest;
import org.service.booking.entity.Booking;
import org.service.booking.mapper.BookingMapper;
import org.service.booking.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@RequestBody CreateBookingRequest request) {
        log.info("Creating booking for user ID: {}, room ID: {}", request.getUserId(), request.getRoomId());

        Booking booking = bookingMapper.toEntity(request);
        Booking createdBooking = bookingService.createBooking(booking);

        // Сразу пытаемся подтвердить бронирование
        try {
            Booking confirmedBooking = bookingService.confirmBooking(createdBooking.getId());
            log.info("Booking successfully confirmed: {}", confirmedBooking.getId());
            return ResponseEntity.ok(bookingMapper.toDTO(confirmedBooking));
        } catch (Exception e) {
            log.error("Failed to confirm booking: {}", e.getMessage());
            // Возвращаем бронирование даже если подтверждение не удалось
            return ResponseEntity.ok(bookingMapper.toDTO(createdBooking));
        }
    }

    @GetMapping
    public ResponseEntity<List<BookingDTO>> getUserBookings(@RequestParam("userId") Long userId) {
        List<BookingDTO> bookings = bookingService.getUserBookings(userId).stream()
                .map(bookingMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable("id") Long id) {
        log.info("Getting booking by ID: {}", id);
        try {
            Booking booking = bookingService.getBookingById(id);
            return ResponseEntity.ok(bookingMapper.toDTO(booking));
        } catch (Exception e) {
            log.error("Error getting booking by ID: {}", id, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable("id") Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<BookingDTO> confirmBooking(@PathVariable("id") Long id) {
        Booking confirmedBooking = bookingService.confirmBooking(id);
        return ResponseEntity.ok(bookingMapper.toDTO(confirmedBooking));
    }
}