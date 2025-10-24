package org.service.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.service.booking.client.HotelServiceClient;
import org.service.booking.dto.RoomDTO;
import org.service.booking.entity.Booking;
import org.service.booking.entity.User;
import org.service.booking.exception.BookingNotFoundException;
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
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HotelServiceClient hotelServiceClient;

    @InjectMocks
    private BookingService bookingService;

    private User testUser;
    private RoomDTO testRoom;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role("USER")
                .build();

        testRoom = RoomDTO.builder()
                .id(1L)
                .roomNumber("101")
                .roomType("STANDARD")
                .price(java.math.BigDecimal.valueOf(100.0))
                .capacity(2)
                .available(true)
                .build();
    }

    @Test
    void createBookingWithRoomSelection_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(hotelServiceClient.checkRoomAvailability(1L, startDate, endDate)).thenReturn(true);
        when(hotelServiceClient.lockRoom(1L, startDate, endDate)).thenReturn(true);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(1L);
            return booking;
        });

        // Act
        Booking result = bookingService.createBookingWithRoomSelection("testuser", 1L, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getRoomId());
        assertEquals(testUser, result.getUser());
        assertEquals(Booking.BookingStatus.CONFIRMED, result.getStatus());

        verify(hotelServiceClient).checkRoomAvailability(1L, startDate, endDate);
        verify(hotelServiceClient).lockRoom(1L, startDate, endDate);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBookingWithRoomSelection_RoomNotAvailable_ThrowsException() {
        // Arrange
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(hotelServiceClient.checkRoomAvailability(1L, startDate, endDate)).thenReturn(false);

        // Act & Assert
        assertThrows(RoomNotAvailableException.class, () -> {
            bookingService.createBookingWithRoomSelection("testuser", 1L, startDate, endDate);
        });

        verify(hotelServiceClient, never()).lockRoom(anyLong(), any(), any());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBookingWithAutoSelect_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);
        List<RoomDTO> availableRooms = List.of(testRoom);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(hotelServiceClient.findAvailableRooms(startDate, endDate)).thenReturn(availableRooms);
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
        assertEquals(1L, result.getRoomId());
        assertEquals(testUser, result.getUser());

        verify(hotelServiceClient).findAvailableRooms(startDate, endDate);
        verify(hotelServiceClient).lockRoom(1L, startDate, endDate);
    }

    @Test
    void createBookingWithAutoSelect_NoRoomsAvailable_ThrowsException() {
        // Arrange
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(hotelServiceClient.findAvailableRooms(startDate, endDate)).thenReturn(List.of());

        // Act & Assert
        assertThrows(RoomNotAvailableException.class, () -> {
            bookingService.createBookingWithAutoSelect("testuser", startDate, endDate);
        });

        verify(hotelServiceClient, never()).lockRoom(anyLong(), any(), any());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_Success() {
        // Arrange
        Long bookingId = 1L;
        Booking booking = Booking.builder()
                .id(bookingId)
                .user(testUser)
                .roomId(1L)
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(4))
                .status(Booking.BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(bookingRepository.findByIdAndUser(bookingId, testUser)).thenReturn(Optional.of(booking));

        // Act
        bookingService.cancelBooking(bookingId, "testuser");

        // Assert
        assertEquals(Booking.BookingStatus.CANCELLED, booking.getStatus());
        verify(bookingRepository).save(booking);
        verify(hotelServiceClient).releaseRoom(1L, booking.getStartDate(), booking.getEndDate());
    }

    @Test
    void cancelBooking_BookingNotFound_ThrowsException() {
        // Arrange
        Long bookingId = 1L;
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(bookingRepository.findByIdAndUser(bookingId, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BookingNotFoundException.class, () -> {
            bookingService.cancelBooking(bookingId, "testuser");
        });

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(hotelServiceClient, never()).releaseRoom(anyLong(), any(), any());
    }

    @Test
    void getUserBookings_Success() {
        // Arrange
        List<Booking> expectedBookings = List.of(
                Booking.builder().id(1L).user(testUser).build(),
                Booking.builder().id(2L).user(testUser).build()
        );

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(bookingRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(expectedBookings);

        // Act
        List<Booking> result = bookingService.getUserBookings("testuser");

        // Assert
        assertEquals(2, result.size());
        verify(bookingRepository).findByUserOrderByCreatedAtDesc(testUser);
    }

    @Test
    void validateDates_InvalidDates_ThrowsException() {
        // Past start date
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.createBookingWithRoomSelection("testuser", 1L,
                        LocalDate.now().minusDays(1), LocalDate.now().plusDays(1))
        );

        // End date before start date
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.createBookingWithRoomSelection("testuser", 1L,
                        LocalDate.now().plusDays(3), LocalDate.now().plusDays(1))
        );

        // Booking too far in advance
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.createBookingWithRoomSelection("testuser", 1L,
                        LocalDate.now().plusYears(2), LocalDate.now().plusYears(2).plusDays(1))
        );
    }
}