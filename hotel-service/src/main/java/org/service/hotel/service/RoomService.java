package org.service.hotel.service;

import lombok.RequiredArgsConstructor;
import org.service.hotel.dto.RoomAvailabilityRequest;
import org.service.hotel.entity.Room;
import org.service.hotel.entity.RoomBooking;
import org.service.hotel.exception.RoomNotAvailableException;
import org.service.hotel.repository.RoomBookingRepository;
import org.service.hotel.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelService hotelService;
    private final RoomBookingRepository roomBookingRepository;

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
     * Подтверждает бронирование (PENDING → CONFIRMED)
     */
    public RoomBooking confirmBooking(Long bookingId) {
        RoomBooking booking = roomBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != RoomBooking.BookingStatus.PENDING) {
            throw new RuntimeException("Booking is not in PENDING status");
        }

        // Проверяем что номер все еще доступен
        if (!isRoomAvailableForDates(booking.getRoomId(), booking.getStartDate(), booking.getEndDate())) {
            throw new RoomNotAvailableException("Room is no longer available for the requested dates");
        }

        booking.setStatus(RoomBooking.BookingStatus.CONFIRMED);

        // Увеличиваем счетчик бронирований комнаты
        Room room = getRoomById(booking.getRoomId());
        room.setTimesBooked(room.getTimesBooked() + 1);
        roomRepository.save(room);

        return roomBookingRepository.save(booking);
    }

    /**
     * Отменяет бронирование
     */
    public RoomBooking cancelBooking(Long bookingId) {
        RoomBooking booking = roomBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(RoomBooking.BookingStatus.CANCELLED);
        return roomBookingRepository.save(booking);
    }

    public boolean isRoomAvailableForDates(Long roomId, LocalDate startDate, LocalDate endDate) {
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

        // Проверяем что комната существует
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        // Проверяем доступность комнаты (используем Boolean available)
        if (!room.getAvailable()) { // или room.getAvailable() != null && room.getAvailable()
            return false;
        }

        // Реальная проверка: нет ли подтвержденных бронирований на эти даты
        return !roomBookingRepository.existsConfirmedBookingForRoomInDateRange(roomId, startDate, endDate);
    }

    /**
     * Создает временную бронь (PENDING статус)
     */
    public RoomBooking createTemporaryBooking(Long roomId, LocalDate startDate, LocalDate endDate, String correlationId) {
        Room room = getRoomById(roomId);

        if (!isRoomAvailableForDates(roomId, startDate, endDate)) {
            throw new RoomNotAvailableException(
                    String.format("Room %d is not available for dates %s to %s", roomId, startDate, endDate)
            );
        }

        RoomBooking temporaryBooking = RoomBooking.builder()
                .roomId(roomId)
                .startDate(startDate)
                .endDate(endDate)
                .status(RoomBooking.BookingStatus.PENDING)
                .correlationId(correlationId)
                .build();

        return roomBookingRepository.save(temporaryBooking);
    }

    /**
     * Отменяет бронирование по correlationId
     */
    public void cancelBookingByCorrelationId(String correlationId) {
        roomBookingRepository.findAll().stream()
                .filter(booking -> correlationId.equals(booking.getCorrelationId()))
                .filter(booking -> booking.getStatus() == RoomBooking.BookingStatus.PENDING)
                .forEach(booking -> {
                    booking.setStatus(RoomBooking.BookingStatus.CANCELLED);
                    roomBookingRepository.save(booking);
                });
    }

    /**
     * Получает доступные номера для указанных дат
     */
    public List<Room> getAvailableRoomsForDates(LocalDate startDate, LocalDate endDate) {
        return roomRepository.findByAvailableTrue().stream()
                .filter(room -> isRoomAvailableForDates(room.getId(), startDate, endDate))
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
    }
}