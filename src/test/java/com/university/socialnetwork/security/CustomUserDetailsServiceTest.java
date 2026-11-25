package com.university.socialnetwork.security;

import com.university.socialnetwork.entity.Role;
import com.university.socialnetwork.entity.User;
import com.university.socialnetwork.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsernameShouldReturnUserDetailsWithRoleAuthority() {
        User user = User.builder()
                .id(2L)
                .firstName("Alan")
                .lastName("Turing")
                .email("alan@university.edu")
                .password("$2a$10$encoded")
                .role(Role.ADMIN)
                .faculty("Mathematics")
                .studyYear(4)
                .build();

        when(userRepository.findByEmail("alan@university.edu")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("alan@university.edu");

        assertThat(details.getUsername()).isEqualTo("alan@university.edu");
        assertThat(details.getPassword()).isEqualTo("$2a$10$encoded");
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsernameShouldThrowWhenEmailNotFound() {
        when(userRepository.findByEmail("missing@university.edu")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("missing@university.edu"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}