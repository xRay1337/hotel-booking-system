package org.service.hotel.service;

import lombok.RequiredArgsConstructor;
import org.service.hotel.entity.Room;
import org.service.hotel.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelService hotelService;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getRoomsByHotelId(Long hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }

    public List<Room> getAvailableRooms() {
        return roomRepository.findByAvailableTrue();
    }

    public List<Room> getRecommendedRooms() {
        return roomRepository.findAvailableRoomsOrderByTimesBooked();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
    }

    public Room createRoom(Room room) {
        // Проверяем, что отель существует
        hotelService.getHotelById(room.getHotel().getId());
        return roomRepository.save(room);
    }

    public Room updateRoom(Long id, Room roomDetails) {
        Room room = getRoomById(id);
        room.setNumber(roomDetails.getNumber());
        room.setType(roomDetails.getType());
        room.setPrice(roomDetails.getPrice());
        room.setAvailable(roomDetails.getAvailable());
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        Room room = getRoomById(id);
        roomRepository.delete(room);
    }

    public Room confirmAvailability(Long roomId) {
        Room room = getRoomById(roomId);
        if (!room.getAvailable()) {
            throw new RuntimeException("Room is not available");
        }
        // Временная блокировка номера
        room.setAvailable(false);
        room.setTimesBooked(room.getTimesBooked() + 1);
        return roomRepository.save(room);
    }

    public Room releaseRoom(Long roomId) {
        Room room = getRoomById(roomId);
        // Снимаем блокировку
        room.setAvailable(true);
        return roomRepository.save(room);
    }
}