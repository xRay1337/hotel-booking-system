package org.service.hotel.mapper;

import org.service.hotel.dto.RoomDTO;
import org.service.hotel.dto.CreateRoomRequest;
import org.service.hotel.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(source = "hotel.id", target = "hotelId")
    RoomDTO toDTO(Room room);

    @Mapping(source = "hotelId", target = "hotel.id")
    @Mapping(target = "timesBooked", ignore = true)
    Room toEntity(RoomDTO roomDTO);

    @Mapping(source = "hotelId", target = "hotel.id")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timesBooked", constant = "0")
    Room toEntity(CreateRoomRequest request);
}