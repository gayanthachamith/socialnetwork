package com.university.socialnetwork.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Faculty is required")
    @Size(max = 150)
    private String faculty;

    @NotNull(message = "Study year is required")
    @Min(value = 1, message = "Study year must be at least 1")
    @Max(value = 10, message = "Study year must be at most 10")
    private Integer studyYear;
}