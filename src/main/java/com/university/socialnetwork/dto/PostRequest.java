package com.university.socialnetwork.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequest {

    @NotBlank(message = "Post content cannot be blank")
    @Size(min = 1, max = 1000, message = "Post content must be between 1 and 1000 characters")
    private String content;
}