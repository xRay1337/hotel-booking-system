package org.service.hotel.controller;

import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/hotels/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoomMapper roomMapper;

    @PostMapping()
    public ResponseEntity<RoomDTO> createRoom(@RequestBody CreateRoomRequest request) {
        Room createdRoom = roomService.createRoomByRequest(request);
        return ResponseEntity.ok(roomMapper.toDTO(createdRoom));
    }

    @GetMapping()
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        List<RoomDTO> rooms = roomService.getAllRooms().stream()
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

    /**
     * Подтвердить доступность номера на запрошенные даты
     * INTERNAL - используется в шаге согласованности
     */
    @PostMapping("/{id}/confirm-availability")
    public ResponseEntity<Boolean> confirmAvailability(
            @PathVariable("id") Long id,
            @RequestBody RoomAvailabilityRequest request) {

        boolean available = roomService.confirmAvailability(id, request);
        return ResponseEntity.ok(available);
    }

    /**
     * Компенсирующее действие: снять временную блокировку
     * INTERNAL - маршрут не публикуется через Gateway
     */
    @PostMapping("/{id}/release")
    public ResponseEntity<Void> releaseRoom(@PathVariable("id") Long id) {
        roomService.releaseRoom(id);
        return ResponseEntity.ok().build();
    }
}