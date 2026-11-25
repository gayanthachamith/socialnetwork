package com.university.socialnetwork.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @Size(max = 500, message = "Bio must be at most 500 characters")
    private String bio;

    @NotBlank(message = "Faculty is required")
    @Size(max = 150)
    private String faculty;

    @NotNull(message = "Study year is required")
    @Min(value = 1)
    @Max(value = 10)
    private Integer studyYear;
}