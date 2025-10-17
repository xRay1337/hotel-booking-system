package org.service.hotel.controller;

import lombok.RequiredArgsConstructor;
import org.service.hotel.dto.CreateHotelRequest;
import org.service.hotel.dto.HotelDTO;
import org.service.hotel.entity.Hotel;
import org.service.hotel.mapper.HotelMapper;
import org.service.hotel.service.HotelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;
    private final HotelMapper hotelMapper;

    @GetMapping
    public ResponseEntity<List<HotelDTO>> getAllHotels() {
        List<HotelDTO> hotels = hotelService.getAllHotels().stream()
                .map(hotelMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(hotels);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelDTO> getHotelById(@PathVariable Long id) {
        Hotel hotel = hotelService.getHotelById(id);
        return ResponseEntity.ok(hotelMapper.toDTO(hotel));
    }

    @PostMapping
    public ResponseEntity<HotelDTO> createHotel(@RequestBody CreateHotelRequest request) {
        Hotel hotel = hotelMapper.toEntity(request);
        Hotel createdHotel = hotelService.createHotel(hotel);
        return ResponseEntity.ok(hotelMapper.toDTO(createdHotel));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelDTO> updateHotel(@PathVariable Long id, @RequestBody CreateHotelRequest request) {
        Hotel updatedHotel = hotelService.updateHotel(id, hotelMapper.toEntity(request));
        return ResponseEntity.ok(hotelMapper.toDTO(updatedHotel));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }
}