package org.service.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.booking.client.HotelServiceClient;
import org.service.booking.dto.RoomDTO;
import org.service.booking.entity.Booking;
import org.service.booking.entity.User;
import org.service.booking.exception.BookingNotFoundException;
import org.service.booking.exception.RoomNotAvailableException;
import org.service.booking.repository.BookingRepository;
import org.service.booking.repository.UserRepository;
import org.service.booking.util.CorrelationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final HotelServiceClient hotelServiceClient;

    @Transactional
    public Booking createBookingWithAutoSelect(String username, LocalDate startDate, LocalDate endDate) {
        log.info("Auto-selecting room for user: {}, dates: {} to {}", username, startDate, endDate);

        validateDates(startDate, endDate);
        User user = getUserByUsername(username);

        List<RoomDTO> availableRooms = hotelServiceClient.findAvailableRooms(startDate, endDate);

        if (availableRooms.isEmpty()) {
            throw new RoomNotAvailableException("No available rooms for the selected dates");
        }

        RoomDTO selectedRoom = availableRooms.get(0);
        log.info("Auto-selected room: {} for user: {}", selectedRoom.getId(), username);

        return createBooking(user, selectedRoom.getId(), startDate, endDate);
    }

    @Transactional
    public Booking createBookingWithRoomSelection(String username, Long roomId, LocalDate startDate, LocalDate endDate) {
        log.info("Creating booking for user: {}, room: {}, dates: {} to {}", username, roomId, startDate, endDate);

        validateDates(startDate, endDate);
        User user = getUserByUsername(username);

        boolean isAvailable = hotelServiceClient.checkRoomAvailability(roomId, startDate, endDate);

        if (!isAvailable) {
            throw new RoomNotAvailableException("Room " + roomId + " is not available for the selected dates");
        }

        return createBooking(user, roomId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Booking> getUserBookings(String username) {
        log.info("Getting bookings for user: {}", username);
        User user = getUserByUsername(username);
        return bookingRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public Booking getBookingByIdAndUser(Long id, String username) {
        log.info("Getting booking: {} for user: {}", id, username);
        User user = getUserByUsername(username);

        return bookingRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> {
                    log.warn("Booking not found or access denied: {} for user: {}", id, username);
                    return new BookingNotFoundException("Booking not found with id: " + id + " for user: " + username);
                });
    }

    @Transactional
    public void cancelBooking(Long id, String username) {
        log.info("Cancelling booking: {} for user: {}", id, username);
        User user = getUserByUsername(username);

        Booking booking = bookingRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));

        validateCancellation(booking);

        hotelServiceClient.releaseRoom(booking.getRoomId(), booking.getStartDate(), booking.getEndDate());

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        log.info("Booking cancelled successfully: {}", id);
    }

    @Transactional(readOnly = true)
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));
    }

    // Вспомогательные методы
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }

        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        if (startDate.isAfter(LocalDate.now().plusYears(1))) {
            throw new IllegalArgumentException("Booking cannot be made more than 1 year in advance");
        }

        if (startDate.plusDays(30).isBefore(endDate)) {
            throw new IllegalArgumentException("Booking cannot exceed 30 days");
        }
    }

    private void validateCancellation(Booking booking) {
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot cancel completed booking");
        }

        if (booking.getStartDate().isBefore(LocalDate.now().plusDays(1))) {
            throw new IllegalStateException("Cannot cancel booking less than 24 hours before check-in");
        }
    }

    private Booking createBooking(User user, Long roomId, LocalDate startDate, LocalDate endDate) {
        boolean roomLocked = hotelServiceClient.lockRoom(roomId, startDate, endDate);

        if (!roomLocked) {
            throw new RoomNotAvailableException("Failed to lock room " + roomId);
        }

        try {
            Booking booking = Booking.builder()
                    .user(user)
                    .roomId(roomId)
                    .startDate(startDate)
                    .endDate(endDate)
                    .status(Booking.BookingStatus.CONFIRMED)
                    .correlationId(getOrGenerateCorrelationId())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Booking savedBooking = bookingRepository.save(booking);
            log.info("Booking created successfully: {}", savedBooking.getId());
            return savedBooking;

        } catch (Exception e) {
            hotelServiceClient.releaseRoom(roomId, startDate, endDate);
            throw new RuntimeException("Failed to create booking: " + e.getMessage(), e);
        }
    }

    private String getOrGenerateCorrelationId() {
        String correlationId = CorrelationContext.getCorrelationId();
        return correlationId != null ? correlationId : java.util.UUID.randomUUID().toString();
    }
}