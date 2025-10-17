package org.service.hotel.controller;

import org.service.hotel.dto.CreateRoomRequest;
import org.service.hotel.dto.RoomAvailabilityRequest;
import org.service.hotel.dto.RoomDTO;
import org.service.hotel.entity.Room;
import org.service.hotel.mapper.RoomMapper;
import org.service.hotel.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;
    private final RoomMapper roomMapper;

    public RoomController(RoomService roomService, RoomMapper roomMapper) {
        this.roomService = roomService;
        this.roomMapper = roomMapper;
    }

    @GetMapping
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        List<RoomDTO> rooms = roomService.getAllRooms().stream()
                .map(roomMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<RoomDTO>> getRoomsByHotel(@PathVariable Long hotelId) {
        List<RoomDTO> rooms = roomService.getRoomsByHotelId(hotelId).stream()
                .map(roomMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/available")
    public ResponseEntity<List<RoomDTO>> getAvailableRooms() {
        List<RoomDTO> rooms = roomService.getAvailableRooms().stream()
                .map(roomMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/recommend")
    public ResponseEntity<List<RoomDTO>> getRecommendedRooms() {
        List<RoomDTO> rooms = roomService.getRecommendedRooms().stream()
                .map(roomMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long id) {
        Room room = roomService.getRoomById(id);
        return ResponseEntity.ok(roomMapper.toDTO(room));
    }

    @PostMapping
    public ResponseEntity<RoomDTO> createRoom(@RequestBody CreateRoomRequest request) {
        Room room = roomMapper.toEntity(request);
        Room createdRoom = roomService.createRoom(room);
        return ResponseEntity.ok(roomMapper.toDTO(createdRoom));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable Long id, @RequestBody CreateRoomRequest request) {
        Room updatedRoom = roomService.updateRoom(id, roomMapper.toEntity(request));
        return ResponseEntity.ok(roomMapper.toDTO(updatedRoom));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    // Внутренние эндпоинты для Booking Service
    @PostMapping("/{id}/confirm-availability")
    public ResponseEntity<RoomDTO> confirmAvailability(@PathVariable Long id,
                                                       @RequestBody RoomAvailabilityRequest request) {
        Room room = roomService.confirmAvailability(id);
        return ResponseEntity.ok(roomMapper.toDTO(room));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<RoomDTO> releaseRoom(@PathVariable Long id) {
        Room room = roomService.releaseRoom(id);
        return ResponseEntity.ok(roomMapper.toDTO(room));
    }
}