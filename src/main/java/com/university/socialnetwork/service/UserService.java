package com.university.socialnetwork.service;

import com.university.socialnetwork.dto.RegisterRequest;
import com.university.socialnetwork.dto.UpdateProfileRequest;
import com.university.socialnetwork.dto.UserDto;
import com.university.socialnetwork.entity.User;

import java.util.List;

public interface UserService {

    UserDto register(RegisterRequest request);

    User findByEmail(String email);

    UserDto getProfile(Long userId);

    UserDto updateProfile(String email, UpdateProfileRequest request);

    List<UserDto> findAllUsers();
}