package org.service.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.booking.client.HotelServiceClient;
import org.service.booking.dto.RoomAvailabilityRequest;
import org.service.booking.dto.BookingDTO;
import org.service.booking.dto.BookingRequest;
import org.service.booking.dto.UserDTO;
import org.service.booking.entity.Booking;
import org.service.booking.entity.User;
import org.service.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    public BookingDTO createBooking(BookingRequest request) {
        try {
            // Используем метод который возвращает User entity
            User user = userService.getUserById(request.getUserId());

            Booking booking = Booking.builder()
                    .user(user)  // User entity
                    .roomId(request.getRoomId())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .status(Booking.BookingStatus.PENDING)
                    .correlationId(UUID.randomUUID().toString())
                    .createdAt(LocalDateTime.now())
                    .build();

            Booking savedBooking = bookingRepository.save(booking);
            return bookingMapper.toDTO(savedBooking);

        } catch (Exception e) {
            log.error("Error creating booking", e);
            throw new RuntimeException("Failed to create booking: " + e.getMessage());
        }
    }

    public void cancelBooking(Long id) {
        Booking booking = getBookingById(id);
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking cancelled with ID: {}", id);
    }

    /**
     * Подтверждение бронирования (PENDING → CONFIRMED)
     * С проверкой доступности номера через Hotel Service
     */
    public Booking confirmBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        log.info("Attempting to confirm booking ID: {}, Room: {}, Dates: {} to {}",
                bookingId, booking.getRoomId(), booking.getStartDate(), booking.getEndDate());

        // Проверяем, что бронирование в статусе PENDING
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Booking is not in PENDING status. Current status: " + booking.getStatus());
        }

        try {
            // Создаем запрос на подтверждение доступности
            RoomAvailabilityRequest request = RoomAvailabilityRequest.builder()
                    .startDate(booking.getStartDate())
                    .endDate(booking.getEndDate())
                    .correlationId(booking.getCorrelationId())
                    .build();

            log.info("Calling Hotel Service to confirm availability for room ID: {}", booking.getRoomId());

            // Вызываем Hotel Service для подтверждения доступности
            hotelServiceClient.confirmRoomAvailability(booking.getRoomId(), request);

            // Если успешно - подтверждаем бронирование
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            Booking confirmedBooking = bookingRepository.save(booking);
            log.info("✅ Booking confirmed with ID: {}", bookingId);

            return confirmedBooking;

        } catch (Exception e) {
            log.error("❌ Error confirming booking ID: {}", bookingId, e);

            // При ошибке - отменяем бронирование
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            log.info("📝 Booking cancelled due to error, ID: {}", bookingId);

            // Пытаемся освободить номер в Hotel Service (компенсирующее действие)
            try {
                hotelServiceClient.releaseRoom(booking.getRoomId());
                log.info("🔓 Room released for booking ID: {}", bookingId);
            } catch (Exception ex) {
                log.error("⚠️ Failed to release room for booking ID {}: {}", bookingId, ex.getMessage());
            }

            throw new RuntimeException("Failed to confirm booking: " + e.getMessage());
        }
    }

    /**
     * Полная сага создания бронирования
     * Создает бронирование и сразу пытается его подтвердить
     */
    public Booking createBookingWithSaga(Booking bookingRequest) {
        log.info("Starting booking saga for user: {}, room: {}",
                bookingRequest.getUser().getId(), bookingRequest.getRoomId());

        // Шаг 1: Создаем бронирование в статусе PENDING
        Booking pendingBooking = createBooking(bookingRequest);

        // Шаг 2: Пытаемся подтвердить бронирование
        try {
            Booking confirmedBooking = confirmBooking(pendingBooking.getId());
            log.info("✅ Booking saga completed successfully. Booking ID: {}", confirmedBooking.getId());
            return confirmedBooking;
        } catch (Exception e) {
            log.error("❌ Booking saga failed. Booking ID: {}", pendingBooking.getId(), e);
            // Бронирование уже отменено в методе confirmBooking
            throw new RuntimeException("Booking failed: " + e.getMessage());
        }
    }

    /**
     * Получить бронирования по correlationId (для проверки идемпотентности)
     */
    public Booking getBookingByCorrelationId(String correlationId) {
        return bookingRepository.findByCorrelationId(correlationId)
                .orElseThrow(() -> new RuntimeException("Booking not found with correlationId: " + correlationId));
    }

    // Вспомогательные методы для тестирования
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