package org.service.hotel.service;

import lombok.RequiredArgsConstructor;
import org.service.hotel.dto.RoomAvailabilityRequest;
import org.service.hotel.entity.Room;
import org.service.hotel.exception.RoomNotAvailableException;
import org.service.hotel.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    public Room confirmAvailability(Long roomId, RoomAvailabilityRequest request) {
        Room room = getRoomById(roomId);

        // Проверяем базовую доступность номера
        if (!room.getAvailable()) {
            throw new RoomNotAvailableException("Room is not available for booking");
        }

        // Проверяем доступность на конкретные даты
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (!isRoomAvailableForDates(roomId, startDate, endDate)) {
            throw new RoomNotAvailableException(
                    String.format("Room is not available for dates %s to %s", startDate, endDate)
            );
        }

        // Временная блокировка номера на период проверки
        // В реальной системе здесь была бы временная блокировка в кэше
        // room.setAvailable(false); // Не блокируем полностью, только на даты

        // Увеличиваем счетчик бронирований для алгоритма рекомендаций
        room.setTimesBooked(room.getTimesBooked() + 1);

        return roomRepository.save(room);
    }

    public Room releaseRoom(Long roomId) {
        Room room = getRoomById(roomId);
        // Снимаем временную блокировку
        // В реальной системе здесь разблокировали бы даты в кэше
        // room.setAvailable(true); // Не меняем общую доступность

        return roomRepository.save(room);
    }

    /**
     * Проверяет доступность номера на указанные даты
     * В реальной системе здесь была бы проверка в таблице бронирований
     */
    private boolean isRoomAvailableForDates(Long roomId, LocalDate startDate, LocalDate endDate) {
        // Временная реализация - всегда возвращает true
        // В реальном приложении здесь была бы проверка:
        // - нет ли подтвержденных бронирований на эти даты
        // - нет ли временных блокировок

        // Валидация дат
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }

        // TODO: Реальная проверка доступности дат
        // return !bookingRepository.existsByRoomIdAndDatesOverlap(roomId, startDate, endDate);

        return true; // Временная заглушка
    }

    /**
     * Получает доступные номера для указанных дат
     */
    public List<Room> getAvailableRoomsForDates(LocalDate startDate, LocalDate endDate) {
        // В реальной системе здесь была бы сложная логика проверки доступности
        return roomRepository.findByAvailableTrue().stream()
                .filter(room -> isRoomAvailableForDates(room.getId(), startDate, endDate))
                .toList();
    }

    /**
     * Получает рекомендованные номера для указанных дат
     */
    public List<Room> getRecommendedRoomsForDates(LocalDate startDate, LocalDate endDate) {
        return getAvailableRoomsForDates(startDate, endDate).stream()
                .sorted((r1, r2) -> {
                    // Сначала по times_booked (меньше бронирований - выше в списке)
                    int timesBookedCompare = Integer.compare(r1.getTimesBooked(), r2.getTimesBooked());
                    if (timesBookedCompare != 0) {
                        return timesBookedCompare;
                    }
                    // При равенстве - по ID
                    return Long.compare(r1.getId(), r2.getId());
                })
                .toList();
    }
}