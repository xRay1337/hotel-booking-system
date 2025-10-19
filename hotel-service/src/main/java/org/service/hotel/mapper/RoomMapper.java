package org.service.hotel.mapper;

import org.service.hotel.dto.RoomDTO;
import org.service.hotel.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(source = "hotel.id", target = "hotelId")
    RoomDTO toDTO(Room room);
}