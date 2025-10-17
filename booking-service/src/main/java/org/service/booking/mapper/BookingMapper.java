package org.service.booking.mapper;

import org.service.booking.dto.BookingDTO;
import org.service.booking.dto.CreateBookingRequest;
import org.service.booking.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "user.id", target = "userId")
    BookingDTO toDTO(Booking booking);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "correlationId", ignore = true)
    Booking toEntity(BookingDTO bookingDTO);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "correlationId", ignore = true)
    Booking toEntity(CreateBookingRequest request);
}