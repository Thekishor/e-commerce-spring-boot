package com.user_service.mapper;

import com.user_service.dto.UserRequest;
import com.user_service.dto.UserResponse;
import com.user_service.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User mapUserRequestToUserEntity(UserRequest userRequest);

    @Mapping(target = "userId", source = "user.userId")
    UserResponse mapUserEntityToUserResponse(User user);
}
