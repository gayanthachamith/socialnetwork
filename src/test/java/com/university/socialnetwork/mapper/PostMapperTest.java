package com.university.socialnetwork.mapper;

import com.university.socialnetwork.dto.PostDto;
import com.university.socialnetwork.entity.Post;
import com.university.socialnetwork.entity.Role;
import com.university.socialnetwork.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PostMapperTest {

    private final PostMapper postMapper = new PostMapper();

    @Test
    void shouldMapPostAndEngagementCountsToPostDto() {
        User author = User.builder()
                .id(10L)
                .firstName("Grace")
                .lastName("Hopper")
                .email("grace@university.edu")
                .password("secret")
                .role(Role.STUDENT)
                .faculty("Engineering")
                .studyYear(3)
                .build();

        Instant now = Instant.parse("2026-09-02T09:30:00Z");
        Post post = Post.builder()
                .id(5L)
                .content("Compilers study group tonight")
                .createdAt(now)
                .updatedAt(now)
                .author(author)
                .build();

        PostDto dto = postMapper.toDto(post, 4L, 2L, true);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getContent()).isEqualTo("Compilers study group tonight");
        assertThat(dto.getAuthorName()).isEqualTo("Grace Hopper");
        assertThat(dto.getAuthorId()).isEqualTo(10L);
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getLikeCount()).isEqualTo(4);
        assertThat(dto.getCommentCount()).isEqualTo(2);
        assertThat(dto.isLikedByCurrentUser()).isTrue();
    }
}