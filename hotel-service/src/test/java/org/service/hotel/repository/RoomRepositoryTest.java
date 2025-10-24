package org.service.hotel.repository;

import org.junit.jupiter.api.Test;
import org.service.hotel.entity.Hotel;
import org.service.hotel.entity.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Test
    void findAvailableRooms() {
        // Arrange
        Hotel hotel = Hotel.builder()
                .name("Test Hotel")
                .address("Test Address")
                .build();
        Hotel savedHotel = hotelRepository.save(hotel);

        Room room1 = Room.builder()
                .hotel(savedHotel)
                .number("101")
                .type("STANDARD")
                .price(100.0)
                .available(true)
                .build();

        Room room2 = Room.builder()
                .hotel(savedHotel)
                .number("102")
                .type("DELUXE")
                .price(150.0)
                .available(false)
                .build();

        roomRepository.save(room1);
        roomRepository.save(room2);

        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);

        // Act
        List<Room> availableRooms = roomRepository.findAvailableRooms(startDate, endDate);

        // Assert
        assertEquals(1, availableRooms.size());
        assertTrue(availableRooms.get(0).getAvailable());
    }

    @Test
    void findByHotelId() {
        // Arrange
        Hotel hotel = Hotel.builder()
                .name("Test Hotel")
                .address("Test Address")
                .build();
        Hotel savedHotel = hotelRepository.save(hotel);

        Room room1 = Room.builder()
                .hotel(savedHotel)
                .number("101")
                .type("STANDARD")
                .price(100.0)
                .available(true)
                .build();

        Room room2 = Room.builder()
                .hotel(savedHotel)
                .number("102")
                .type("DELUXE")
                .price(150.0)
                .available(true)
                .build();

        roomRepository.save(room1);
        roomRepository.save(room2);

        // Act
        List<Room> rooms = roomRepository.findByHotelId(savedHotel.getId());

        // Assert
        assertEquals(2, rooms.size());
        assertTrue(rooms.stream().allMatch(room -> room.getHotel().getId().equals(savedHotel.getId())));
    }

    @Test
    void findByRoomNumberAndHotelId() {
        // Arrange
        Hotel hotel = Hotel.builder()
                .name("Test Hotel")
                .address("Test Address")
                .build();
        Hotel savedHotel = hotelRepository.save(hotel);

        Room room = Room.builder()
                .hotel(savedHotel)
                .number("101")
                .type("STANDARD")
                .price(100.0)
                .available(true)
                .build();
        roomRepository.save(room);

        // Act
        Optional<Room> foundRoom = roomRepository.findByRoomNumberAndHotelId("101", savedHotel.getId());

        // Assert
        assertTrue(foundRoom.isPresent());
        assertEquals("101", foundRoom.get().getNumber());
        assertEquals(savedHotel.getId(), foundRoom.get().getHotel().getId());
    }
}