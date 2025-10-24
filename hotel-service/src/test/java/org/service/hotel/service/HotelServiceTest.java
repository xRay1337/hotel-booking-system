package org.service.hotel.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.service.hotel.dto.CreateHotelRequest;
import org.service.hotel.dto.CreateRoomRequest;
import org.service.hotel.dto.HotelDTO;
import org.service.hotel.dto.RoomDTO;
import org.service.hotel.entity.Hotel;
import org.service.hotel.entity.Room;
import org.service.hotel.exception.RoomNotAvailableException;
import org.service.hotel.exception.RoomNotFoundException;
import org.service.hotel.mapper.HotelMapper;
import org.service.hotel.mapper.RoomMapper;
import org.service.hotel.repository.HotelRepository;
import org.service.hotel.repository.RoomRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelMapper hotelMapper;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private HotelService hotelService;

    @InjectMocks
    private RoomService roomService;

    private Hotel testHotel;
    private Room testRoom;
    private HotelDTO testHotelDTO;
    private RoomDTO testRoomDTO;

    @BeforeEach
    void setUp() {
        testHotel = Hotel.builder()
                .id(1L)
                .name("Test Hotel")
                .address("Test Address")
                .build();

        testRoom = Room.builder()
                .id(1L)
                .hotel(testHotel)
                .number("101")
                .type("STANDARD")
                .price(100.0)
                .available(true)
                .build();

        testHotelDTO = HotelDTO.builder()
                .id(1L)
                .name("Test Hotel")
                .address("Test Address")
                .build();

        testRoomDTO = RoomDTO.builder()
                .id(1L)
                .hotelId(1L)
                .available(true)
                .build();
    }

    @Test
    void createHotel_Success() {
        // Arrange
        CreateHotelRequest request = CreateHotelRequest.builder()
                .name("New Hotel")
                .address("New Address")
                .build();

        when(hotelMapper.toEntity(request)).thenReturn(testHotel);
        when(hotelRepository.save(any(Hotel.class))).thenReturn(testHotel);
        when(hotelMapper.toDTO(testHotel)).thenReturn(testHotelDTO);

        // Act
        Hotel result = hotelService.createHotel(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Hotel", result.getName());
        verify(hotelRepository).save(any(Hotel.class));
    }

    @Test
    void getAllHotels_Success() {
        // Arrange
        List<Hotel> hotels = List.of(testHotel);
        List<HotelDTO> hotelDTOs = List.of(testHotelDTO);

        when(hotelRepository.findAll()).thenReturn(hotels);
        when(hotelMapper.toDTO(testHotel)).thenReturn(testHotelDTO);

        // Act
        List<Hotel> result = hotelService.getAllHotels();

        // Assert
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(hotelRepository).findAll();
    }

    @Test
    void createRoom_Success() {
        // Arrange
        CreateRoomRequest request = CreateRoomRequest.builder()
                .hotelId(1L)
                .number("102")
                .type("DELUXE")
                .price(150.0)
                .build();

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(testHotel));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        when(roomMapper.toDTO(testRoom)).thenReturn(testRoomDTO);

    }

    @Test
    void findAvailableRooms_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);
        List<Room> availableRooms = List.of(testRoom);

        when(roomMapper.toDTO(testRoom)).thenReturn(testRoomDTO);

    }

    @Test
    void checkRoomAvailability_RoomAvailable() {
        // Arrange
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
    }


    @Test
    void lockRoom_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        verify(roomRepository).save(testRoom);
    }

    @Test
    void lockRoom_AlreadyLocked() {
        // Arrange
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void lockRoom_RoomNotFound() {
        // Arrange
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

    }

    @Test
    void releaseRoom_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        // Assert
        verify(roomRepository).save(testRoom);
    }
}