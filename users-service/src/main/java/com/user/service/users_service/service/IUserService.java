package com.user.service.users_service.service;

import com.user.service.users_service.dto.UserDto;
import com.user.service.users_service.model.User;
import com.user.service.users_service.request.CreateUserRequest;
import com.user.service.users_service.request.UpdateUserRequest;

public interface IUserService {

    User getUserById(Long userId);

    User createUser(CreateUserRequest request);

    User updateUser(UpdateUserRequest request, Long userId);

    void deleteUser(Long userId);

    UserDto convertUserToDto(User user);

    User getAuthenticatedUser();


}
