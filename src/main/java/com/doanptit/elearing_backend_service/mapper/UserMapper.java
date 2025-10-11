package com.doanptit.elearing_backend_service.mapper;

import com.doanptit.elearing_backend_service.dto.req.UpdateProfileRequest;
import com.doanptit.elearing_backend_service.dto.res.UserResponseDto;
import com.doanptit.elearing_backend_service.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toUserResponseDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(@MappingTarget User user, UpdateProfileRequest userDto);
}