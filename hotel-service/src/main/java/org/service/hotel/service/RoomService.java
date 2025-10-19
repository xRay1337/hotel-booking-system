package org.service.hotel.service;

import lombok.RequiredArgsConstructor;
import org.service.hotel.dto.CreateRoomRequest;
import org.service.hotel.dto.RoomAvailabilityRequest;
import org.service.hotel.entity.Hotel;
import org.service.hotel.entity.Room;
import org.service.hotel.entity.RoomLockProperties;
import org.service.hotel.exception.RoomNotFoundException;
import org.service.hotel.repository.HotelRepository;
import org.service.hotel.repository.RoomRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final RoomLockProperties roomLockProperties;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getRecommendedRooms() {
        return roomRepository.findAvailableRoomsOrderByTimesBooked();
    }

    public Room createRoomByRequest(CreateRoomRequest request) {
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + request.getHotelId()));

        Room room = new Room();
        room.setNumber(request.getNumber());
        room.setType(request.getType());
        room.setPrice(request.getPrice());
        room.setHotel(hotel);
        room.setAvailable(true);
        room.setTimesBooked(0);

        return roomRepository.save(room);
    }

    /**
     * Подтвердить доступность номера на даты
     */
    public boolean confirmAvailability(Long roomId, RoomAvailabilityRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found with id: " + roomId));

        // Проверяем не заблокирована ли комната (и не истек ли таймаут)
        if (room.getLockedUntil() != null && room.getLockedUntil().isAfter(LocalDateTime.now())) {
            return false; // Уже заблокирована
        }

        room.setAvailable(false);
        room.setLockedUntil(LocalDateTime.now().plusSeconds(roomLockProperties.getTimeoutSeconds()));
        roomRepository.save(room);

        return true;
    }

    /**
     * Снять временную блокировку
     */
    public void releaseRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found with id: " + roomId));

        room.setAvailable(true);
        room.setLockedUntil(null);
        roomRepository.save(room);
    }

    /**
     * Фоновая задача для очистки просроченных блокировок
     */
    @Scheduled(fixedRate = 10000) // Каждые 10 секунд
    @Transactional
    public void cleanupExpiredLocks() {
        List<Room> expiredRooms = roomRepository.findByLockedUntilBefore(LocalDateTime.now());

        for (Room room : expiredRooms) {
            room.setAvailable(true);
            room.setLockedUntil(null);
        }

        roomRepository.saveAll(expiredRooms);
    }
}