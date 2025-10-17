package org.service.booking.mapper;

import org.service.booking.dto.UserDTO;
import org.service.booking.dto.UserRegistrationRequest;
import org.service.booking.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", constant = "USER")
    User toEntity(UserDTO userDTO);

    @Mapping(target = "id", ignore =true)
    @Mapping(target = "role", constant = "USER")
    User toEntity(UserRegistrationRequest request);
}