package com.university.socialnetwork.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {
    private Long id;
    private String content;
    private String authorName;
    private Long authorId;
    private Instant createdAt;
    private long likeCount;
    private long commentCount;
    private boolean likedByCurrentUser;
}