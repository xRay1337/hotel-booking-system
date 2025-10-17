package org.service.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.booking.client.HotelServiceClient;
import org.service.booking.dto.RoomAvailabilityRequest;
import org.service.booking.entity.Booking;
import org.service.booking.entity.User;
import org.service.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final HotelServiceClient hotelServiceClient;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public Booking getBookingById(Long id) {
        try {
            return bookingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        } catch (Exception e) {
            log.error("Error getting booking by ID: {}", id, e);
            throw new RuntimeException("Error retrieving booking: " + e.getMessage());
        }
    }

    public Booking createBooking(Booking booking) {
        // Проверяем, что пользователь существует
        userService.getUserById(booking.getUser().getId());

        // Генерируем correlationId для идемпотентности
        if (booking.getCorrelationId() == null) {
            booking.setCorrelationId(UUID.randomUUID().toString());
        }

        // Проверяем, не обрабатывался ли уже этот запрос
        if (bookingRepository.existsByCorrelationId(booking.getCorrelationId())) {
            throw new RuntimeException("Booking request already processed");
        }

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created with ID: {}, Status: {}", savedBooking.getId(), savedBooking.getStatus());

        return savedBooking;
    }

    public void cancelBooking(Long id) {
        Booking booking = getBookingById(id);
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking cancelled with ID: {}", id);
    }

    public Booking confirmBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        log.info("Attempting to confirm booking ID: {}", bookingId);

        try {
            // ВРЕМЕННО: Просто подтверждаем без вызова Hotel Service
            log.info("SIMULATING Hotel Service call for room ID: {}", booking.getRoomId());

            // TODO: Раскомментировать когда Hotel Service будет готов
            // Создаем запрос на подтверждение доступности
            // RoomAvailabilityRequest request = new RoomAvailabilityRequest(
            //     booking.getStartDate(),
            //     booking.getEndDate(),
            //     booking.getCorrelationId()
            // );

            // Вызываем Hotel Service для подтверждения доступности
            // hotelServiceClient.confirmRoomAvailability(booking.getRoomId(), request).block();

            // Если успешно - подтверждаем бронирование
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            Booking confirmedBooking = bookingRepository.save(booking);
            log.info("Booking confirmed with ID: {}", bookingId);

            return confirmedBooking;

        } catch (Exception e) {
            log.error("Error confirming booking ID: {}", bookingId, e);

            // При ошибке - отменяем бронирование
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            // TODO: Раскомментировать когда Hotel Service будет готов
            // Пытаемся освободить номер в Hotel Service
            // try {
            //     hotelServiceClient.releaseRoom(booking.getRoomId()).block();
            // } catch (Exception ex) {
            //     log.error("Failed to release room: {}", ex.getMessage());
            // }

            throw new RuntimeException("Failed to confirm booking: " + e.getMessage());
        }
    }

    public Booking simulateSuccessfulBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        log.info("Simulating successful booking confirmation for ID: {}", bookingId);

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        Booking confirmedBooking = bookingRepository.save(booking);
        log.info("Booking simulation successful for ID: {}", bookingId);

        return confirmedBooking;
    }

    public Booking simulateFailedBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        log.info("Simulating failed booking confirmation for ID: {}", bookingId);

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        Booking cancelledBooking = bookingRepository.save(booking);
        log.info("Booking simulation failed for ID: {}", bookingId);

        return cancelledBooking;
    }
}