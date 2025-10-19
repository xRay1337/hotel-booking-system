package org.service.hotel.controller;

import org.service.hotel.dto.CreateRoomRequest;
import org.service.hotel.dto.RoomAvailabilityRequest;
import org.service.hotel.dto.RoomDTO;
import org.service.hotel.entity.Room;
import org.service.hotel.entity.RoomBooking;
import org.service.hotel.mapper.RoomMapper;
import org.service.hotel.service.RoomService;
import org.service.hotel.util.CorrelationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;
    private final RoomMapper roomMapper;

    public RoomController(RoomService roomService, RoomMapper roomMapper) {
        this.roomService = roomService;
        this.roomMapper = roomMapper;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")  // ← Чтение для USER и ADMIN
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        List<RoomDTO> rooms = roomService.getAllRooms().stream()
                .map(roomMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/hotel/{hotelId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")  // ← Чтение для USER и ADMIN
    public ResponseEntity<List<RoomDTO>> getRoomsByHotel(@PathVariable Long hotelId) {
        List<RoomDTO> rooms = roomService.getRoomsByHotelId(hotelId).stream()
                .map(roomMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")  // ← Чтение для USER и ADMIN
    public ResponseEntity<List<RoomDTO>> getAvailableRooms() {
        List<RoomDTO> rooms = roomService.getAvailableRooms().stream()
                .map(roomMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/recommend")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")  // ← Чтение для USER и ADMIN
    public ResponseEntity<List<RoomDTO>> getRecommendedRooms() {
        List<RoomDTO> rooms = roomService.getRecommendedRooms().stream()
                .map(roomMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")  // ← Чтение для USER и ADMIN
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long id) {
        Room room = roomService.getRoomById(id);
        return ResponseEntity.ok(roomMapper.toDTO(room));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")  // ← Создание только для ADMIN
    public ResponseEntity<RoomDTO> createRoom(@RequestBody CreateRoomRequest request) {
        Room room = roomMapper.toEntity(request);
        Room createdRoom = roomService.createRoom(room);
        return ResponseEntity.ok(roomMapper.toDTO(createdRoom));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")  // ← Обновление только для ADMIN
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable Long id, @RequestBody CreateRoomRequest request) {
        Room updatedRoom = roomService.updateRoom(id, roomMapper.toEntity(request));
        return ResponseEntity.ok(roomMapper.toDTO(updatedRoom));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")  // ← Удаление только для ADMIN
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")  // ← Для отмены бронирований (USER и ADMIN)
    public ResponseEntity<RoomDTO> releaseRoom(@PathVariable Long id) {
        Room room = roomService.releaseRoom(id);
        return ResponseEntity.ok(roomMapper.toDTO(room));
    }

    @GetMapping("/available/dates")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<RoomDTO>> getAvailableRoomsForDates(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<RoomDTO> rooms = roomService.getAvailableRoomsForDates(startDate, endDate).stream()
                .map(roomMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/recommend/dates")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<RoomDTO>> getRecommendedRoomsForDates(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<RoomDTO> rooms = roomService.getRecommendedRoomsForDates(startDate, endDate).stream()
                .map(roomMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }

    // Внутренние эндпоинты для Booking Service - обновляем
    @PostMapping("/{id}/confirm-room-availability")
    public ResponseEntity<Boolean> confirmRoomAvailability(
            @PathVariable Long id,
            @RequestBody RoomAvailabilityRequest request) {

        try {
            boolean isAvailable = roomService.isRoomAvailableForDates(id, request.getStartDate(), request.getEndDate());

            if (isAvailable) {
                RoomBooking temporaryBooking = roomService.createTemporaryBooking(
                        id, request.getStartDate(), request.getEndDate(), request.getCorrelationId());
                return ResponseEntity.ok(true);
            } else {
                return ResponseEntity.ok(false);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PostMapping("/{id}/release-room")
    public ResponseEntity<Void> releaseRoom(
            @PathVariable Long id,
            @RequestBody RoomAvailabilityRequest request) {

        try {
            roomService.cancelBookingByCorrelationId(request.getCorrelationId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}