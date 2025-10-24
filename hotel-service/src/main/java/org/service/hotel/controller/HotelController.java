package org.service.hotel.controller;

import lombok.RequiredArgsConstructor;
import org.service.hotel.dto.CreateHotelRequest;
import org.service.hotel.dto.HotelDTO;
import org.service.hotel.entity.Hotel;
import org.service.hotel.mapper.HotelMapper;
import org.service.hotel.service.HotelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;
    private final HotelMapper hotelMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HotelDTO> createHotel(@RequestBody CreateHotelRequest request) {
        Hotel hotel = hotelMapper.toEntity(request);
        Hotel createdHotel = hotelService.createHotel(hotel);
        return ResponseEntity.ok(hotelMapper.toDTO(createdHotel));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<HotelDTO>> getAllHotels() {
        List<HotelDTO> hotels = hotelService.getAllHotels().stream()
                .map(hotelMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(hotels);
    }
}