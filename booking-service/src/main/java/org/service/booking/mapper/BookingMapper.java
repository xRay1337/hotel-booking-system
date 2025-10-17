package org.service.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.service.booking.dto.BookingDTO;
import org.service.booking.dto.CreateBookingRequest;
import org.service.booking.entity.Booking;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "user.id", target = "userId")
    BookingDTO toDTO(Booking booking);

    @Mapping(source = "userId", target = "user.id")
    Booking toEntity(BookingDTO bookingDTO);

    // Добавляем метод для CreateBookingRequest
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "correlationId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    Booking toEntity(CreateBookingRequest request);
}