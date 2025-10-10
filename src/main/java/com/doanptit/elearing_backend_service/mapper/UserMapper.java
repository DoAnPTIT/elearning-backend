package com.doanptit.elearing_backend_service.mapper;

import com.doanptit.elearing_backend_service.dto.req.UpdateProfileRequest;
import com.doanptit.elearing_backend_service.dto.res.UserResponseDto;
import com.doanptit.elearing_backend_service.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toUserResponseDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "enrollments", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    @Mapping(target = "createdComments", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedOn", ignore = true)
    void updateUserFromDto(UpdateProfileRequest dto, @MappingTarget User entity);
}