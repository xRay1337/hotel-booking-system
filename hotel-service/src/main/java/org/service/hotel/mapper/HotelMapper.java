package org.service.hotel.mapper;

import org.service.hotel.dto.HotelDTO;
import org.service.hotel.dto.CreateHotelRequest;
import org.service.hotel.entity.Hotel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HotelMapper {

    HotelDTO toDTO(Hotel hotel);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    Hotel toEntity(HotelDTO hotelDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    Hotel toEntity(CreateHotelRequest request);
}