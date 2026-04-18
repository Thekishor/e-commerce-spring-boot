package com.user_service.mapper;

import com.user_service.dto.UserRequest;
import com.user_service.dto.UserResponse;
import com.user_service.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User mapUserRequestToUserEntity(UserRequest userRequest);

    UserResponse mapUserEntityToUserResponse(User user);
}
