package com.university.socialnetwork.repository;

import com.university.socialnetwork.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PostRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Test
    void shouldSaveUserPostAndLikeAndQueryFeedOrderedByCreatedAtDesc() {
        User author = userRepository.save(User.builder()
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@university.edu")
                .password("hashed-password")
                .role(Role.STUDENT)
                .faculty("Computing")
                .studyYear(2)
                .build());

        Post olderPost = postRepository.save(Post.builder()
                .content("First campus post")
                .createdAt(Instant.parse("2026-09-01T10:00:00Z"))
                .updatedAt(Instant.parse("2026-09-01T10:00:00Z"))
                .author(author)
                .build());

        Post newerPost = postRepository.save(Post.builder()
                .content("Second campus post")
                .createdAt(Instant.parse("2026-09-01T11:00:00Z"))
                .updatedAt(Instant.parse("2026-09-01T11:00:00Z"))
                .author(author)
                .build());

        postLikeRepository.save(PostLike.builder()
                .post(newerPost)
                .user(author)
                .build());

        Page<Post> feed = postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10));

        assertThat(feed.getTotalElements()).isEqualTo(2);
        assertThat(feed.getContent().get(0).getId()).isEqualTo(newerPost.getId());
        assertThat(feed.getContent().get(1).getId()).isEqualTo(olderPost.getId());
        assertThat(postLikeRepository.existsByPostIdAndUserId(newerPost.getId(), author.getId())).isTrue();
        assertThat(postLikeRepository.countByPostId(newerPost.getId())).isEqualTo(1);
    }
}