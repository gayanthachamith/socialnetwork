package com.university.socialnetwork.service;

import com.university.socialnetwork.dto.RegisterRequest;
import com.university.socialnetwork.dto.UpdateProfileRequest;
import com.university.socialnetwork.dto.UserDto;
import com.university.socialnetwork.entity.Role;
import com.university.socialnetwork.entity.User;
import com.university.socialnetwork.exception.EmailAlreadyExistsException;
import com.university.socialnetwork.exception.ResourceNotFoundException;
import com.university.socialnetwork.mapper.UserMapper;
import com.university.socialnetwork.repository.UserRepository;
import com.university.socialnetwork.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;
    private RegisterRequest registerRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        UserMapper userMapper = new UserMapper();
        userService = new UserServiceImpl(userRepository, passwordEncoder, userMapper);

        registerRequest = RegisterRequest.builder()
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@university.edu")
                .password("Password123!")
                .faculty("Computing")
                .studyYear(2)
                .build();

        savedUser = User.builder()
                .id(1L)
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@university.edu")
                .password("$2a$10$hashedPasswordValue")
                .role(Role.STUDENT)
                .faculty("Computing")
                .studyYear(2)
                .bio("Analytical engine enthusiast")
                .build();
    }

    @Test
    void registerShouldHashPasswordAssignStudentRoleAndReturnUserDto() {
        when(userRepository.existsByEmail("ada@university.edu")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$10$hashedPasswordValue");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserDto result = userService.register(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User captured = userCaptor.getValue();
        assertThat(captured.getPassword()).isEqualTo("$2a$10$hashedPasswordValue");
        assertThat(captured.getRole()).isEqualTo(Role.STUDENT);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFullName()).isEqualTo("Ada Lovelace");
        assertThat(result.getEmail()).isEqualTo("ada@university.edu");
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("ada@university.edu")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("ada@university.edu");

        verify(userRepository, never()).save(any());
    }

    @Test
    void getProfileShouldReturnUserDtoWhenFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(savedUser));

        UserDto profile = userService.getProfile(1L);

        assertThat(profile.getId()).isEqualTo(1L);
        assertThat(profile.getFullName()).isEqualTo("Ada Lovelace");
        assertThat(profile.getFaculty()).isEqualTo("Computing");
        assertThat(profile.getStudyYear()).isEqualTo(2);
    }

    @Test
    void getProfileShouldThrowWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProfileShouldUpdateBioFacultyAndStudyYear() {
        UpdateProfileRequest updateRequest = UpdateProfileRequest.builder()
                .bio("Updated bio")
                .faculty("Data Science")
                .studyYear(3)
                .build();

        when(userRepository.findByEmail("ada@university.edu")).thenReturn(Optional.of(savedUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateProfile("ada@university.edu", updateRequest);

        assertThat(result.getBio()).isEqualTo("Updated bio");
        assertThat(result.getFaculty()).isEqualTo("Data Science");
        assertThat(result.getStudyYear()).isEqualTo(3);
    }
}