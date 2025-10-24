package org.service.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.service.booking.client.HotelServiceClient;
import org.service.booking.dto.RoomDTO;
import org.service.booking.entity.Booking;
import org.service.booking.entity.User;
import org.service.booking.exception.RoomNotAvailableException;
import org.service.booking.repository.BookingRepository;
import org.service.booking.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceBusinessTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HotelServiceClient hotelServiceClient;

    @InjectMocks
    private BookingService bookingService;

    private User createTestUser() {
        return User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role("USER")
                .build();
    }

    private RoomDTO createTestRoom() {
        return RoomDTO.builder()
                .id(1L)
                .roomNumber("101")
                .roomType("STANDARD")
                .price(java.math.BigDecimal.valueOf(100.0))
                .capacity(2)
                .available(true)
                .build();
    }

    @Test
    void testAutoSelectRoom_Success() {
        // Arrange
        User user = createTestUser();
        RoomDTO room = createTestRoom();
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(hotelServiceClient.findAvailableRooms(startDate, endDate)).thenReturn(List.of(room));
        when(hotelServiceClient.lockRoom(1L, startDate, endDate)).thenReturn(true);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(1L);
            return booking;
        });

        // Act
        Booking result = bookingService.createBookingWithAutoSelect("testuser", startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getRoomId());
        assertEquals(user, result.getUser());
        verify(hotelServiceClient).findAvailableRooms(startDate, endDate);
        verify(hotelServiceClient).lockRoom(1L, startDate, endDate);
    }

    @Test
    void testAutoSelectRoom_NoRoomsAvailable() {
        // Arrange
        User user = createTestUser();
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(hotelServiceClient.findAvailableRooms(startDate, endDate)).thenReturn(List.of());

        // Act & Assert
        assertThrows(RoomNotAvailableException.class, () -> {
            bookingService.createBookingWithAutoSelect("testuser", startDate, endDate);
        });

        verify(hotelServiceClient, never()).lockRoom(anyLong(), any(), any());
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}