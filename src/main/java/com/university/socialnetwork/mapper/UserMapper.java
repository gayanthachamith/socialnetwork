package com.university.socialnetwork.mapper;

import com.university.socialnetwork.dto.UserDto;
import com.university.socialnetwork.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return UserDto.builder()
                .id(user.getId())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .faculty(user.getFaculty())
                .studyYear(user.getStudyYear())
                .bio(user.getBio())
                .build();
    }
}
