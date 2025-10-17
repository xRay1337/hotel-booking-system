package org.service.booking.mapper;

import org.service.booking.dto.CreateBookingRequest;
import org.service.booking.dto.BookingDTO;
import org.service.booking.entity.Booking;
import org.service.booking.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "correlationId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", source = "userId")
    Booking toEntity(CreateBookingRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.username")
    BookingDTO toDTO(Booking booking);

    default User mapUserIdToUser(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = new User();
        user.setId(userId);
        return user;
    }
}