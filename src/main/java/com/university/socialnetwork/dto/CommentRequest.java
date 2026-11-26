package com.university.socialnetwork.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequest {

    @NotBlank(message = "Comment cannot be blank")
    @Size(min = 1, max = 500, message = "Comment must be between 1 and 500 characters")
    private String content;
}