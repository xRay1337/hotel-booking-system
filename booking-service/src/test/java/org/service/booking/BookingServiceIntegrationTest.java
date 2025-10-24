package org.service.booking;

import org.junit.jupiter.api.Test;
import org.service.booking.entity.Booking;
import org.service.booking.entity.User;
import org.service.booking.repository.BookingRepository;
import org.service.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testBookingPersistence() {
        // Arrange
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role("USER")
                .build();

        User savedUser = userRepository.save(user);

        Booking booking = Booking.builder()
                .user(savedUser)
                .roomId(1L)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .status(Booking.BookingStatus.CONFIRMED)
                .correlationId("test-correlation-123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Act
        Booking savedBooking = bookingRepository.save(booking);
        List<Booking> foundBookings = bookingRepository.findByUserOrderByCreatedAtDesc(savedUser);

        // Assert
        assertNotNull(savedBooking.getId());
        assertEquals(1, foundBookings.size());
        assertEquals(savedUser.getId(), foundBookings.get(0).getUser().getId());
        assertEquals(1L, foundBookings.get(0).getRoomId());
        assertEquals(Booking.BookingStatus.CONFIRMED, foundBookings.get(0).getStatus());
    }

    @Test
    void testFindByIdAndUser() {
        // Arrange
        User user = User.builder()
                .username("user1")
                .email("user1@example.com")
                .password("password")
                .role("USER")
                .build();
        User savedUser = userRepository.save(user);

        Booking booking = Booking.builder()
                .user(savedUser)
                .roomId(2L)
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(4))
                .status(Booking.BookingStatus.CONFIRMED)
                .correlationId("test-correlation-456")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Booking savedBooking = bookingRepository.save(booking);

        // Act
        var result = bookingRepository.findByIdAndUser(savedBooking.getId(), savedUser);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(savedBooking.getId(), result.get().getId());
        assertEquals(user.getUsername(), result.get().getUser().getUsername());
    }
}