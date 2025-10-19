package org.service.hotel.service;

import lombok.RequiredArgsConstructor;
import org.service.hotel.dto.CreateRoomRequest;
import org.service.hotel.dto.RoomAvailabilityRequest;
import org.service.hotel.entity.Hotel;
import org.service.hotel.entity.Room;
import org.service.hotel.repository.HotelRepository;
import org.service.hotel.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

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
        //TODO: проверка доступности номера на даты бронирований из request в БД
        return true;
    }

    /**
     * Снять временную блокировку (компенсирующее действие)
     */
    public void releaseRoom(Long roomId) {
        //TODO: здесь снимается блокировка
    }
}