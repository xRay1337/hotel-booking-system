package org.service.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.service.booking.dto.BookingDTO;
import org.service.booking.dto.CreateBookingRequest;
import org.service.booking.entity.Booking;
import org.service.booking.entity.User;
import org.service.booking.mapper.BookingMapper;
import org.service.booking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private BookingMapper bookingMapper;

    private User testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();

    private Booking testBooking = Booking.builder()
            .id(1L)
            .user(testUser)
            .roomId(101L)
            .startDate(LocalDate.now().plusDays(1))
            .endDate(LocalDate.now().plusDays(3))
            .status(Booking.BookingStatus.CONFIRMED)
            .createdAt(LocalDateTime.now())
            .build();

    private BookingDTO testBookingDTO = BookingDTO.builder()
            .id(1L)
            .roomId(101L)
            .startDate(LocalDate.now().plusDays(1))
            .endDate(LocalDate.now().plusDays(3))
            .status("CONFIRMED")
            .build();

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createBooking_AutoSelect_Success() throws Exception {
        // Arrange
        CreateBookingRequest request = CreateBookingRequest.builder()
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .build();

        when(bookingService.createBookingWithAutoSelect(eq("testuser"), any(), any()))
                .thenReturn(testBooking);
        when(bookingMapper.toDTO(testBooking)).thenReturn(testBookingDTO);

        // Act & Assert
        mockMvc.perform(post("/api/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(bookingService).createBookingWithAutoSelect(eq("testuser"), any(), any());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createBooking_RoomSelection_Success() throws Exception {
        // Arrange
        CreateBookingRequest request = CreateBookingRequest.builder()
                .roomId(101L)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .build();

        when(bookingService.createBookingWithRoomSelection(eq("testuser"), eq(101L), any(), any()))
                .thenReturn(testBooking);
        when(bookingMapper.toDTO(testBooking)).thenReturn(testBookingDTO);

        // Act & Assert
        mockMvc.perform(post("/api/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));

        verify(bookingService).createBookingWithRoomSelection(eq("testuser"), eq(101L), any(), any());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getUserBookings_Success() throws Exception {
        // Arrange
        List<Booking> bookings = List.of(testBooking);
        List<BookingDTO> bookingDTOs = List.of(testBookingDTO);

        when(bookingService.getUserBookings("testuser")).thenReturn(bookings);
        when(bookingMapper.toDTO(testBooking)).thenReturn(testBookingDTO);

        // Act & Assert
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(bookingService).getUserBookings("testuser");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getBookingById_Success() throws Exception {
        // Arrange
        when(bookingService.getBookingByIdAndUser(1L, "testuser")).thenReturn(testBooking);
        when(bookingMapper.toDTO(testBooking)).thenReturn(testBookingDTO);

        // Act & Assert
        mockMvc.perform(get("/api/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.roomId").value(101L));

        verify(bookingService).getBookingByIdAndUser(1L, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void cancelBooking_Success() throws Exception {
        // Arrange
        doNothing().when(bookingService).cancelBooking(1L, "testuser");

        // Act & Assert
        mockMvc.perform(delete("/api/bookings/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(bookingService).cancelBooking(1L, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createBooking_InvalidDates_ReturnsBadRequest() throws Exception {
        // Arrange
        CreateBookingRequest request = CreateBookingRequest.builder()
                .roomId(101L)
                .startDate(LocalDate.now().minusDays(1)) // Past date
                .endDate(LocalDate.now().plusDays(3))
                .build();

        when(bookingService.createBookingWithRoomSelection(eq("testuser"), eq(101L), any(), any()))
                .thenThrow(new IllegalArgumentException("Start date cannot be in the past"));

        // Act & Assert
        mockMvc.perform(post("/api/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(bookingService).createBookingWithRoomSelection(eq("testuser"), eq(101L), any(), any());
    }
}