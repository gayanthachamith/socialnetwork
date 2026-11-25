package com.university.socialnetwork.service;


import com.university.socialnetwork.entity.Post;
import com.university.socialnetwork.entity.PostLike;
import com.university.socialnetwork.entity.Role;
import com.university.socialnetwork.entity.User;
import com.university.socialnetwork.exception.ResourceNotFoundException;
import com.university.socialnetwork.repository.PostLikeRepository;
import com.university.socialnetwork.repository.PostRepository;
import com.university.socialnetwork.repository.UserRepository;
import com.university.socialnetwork.service.impl.LikeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LikeServiceImpl likeService;

    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("Gayantha")
                .lastName("Chamith")
                .email("gayantha@university.edu")
                .role(Role.STUDENT)
                .build();

        post = Post.builder()
                .id(10L)
                .author(user)
                .content("Post content")
                .build();
    }

    @Test
    void toggleLikeShouldAddLikeWhenNotLikedYet() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findByEmail("gayantha@university.edu")).thenReturn(Optional.of(user));
        when(postLikeRepository.findByPostIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        boolean liked = likeService.toggleLike(10L, "gayantha@university.edu");

        assertThat(liked).isTrue();
        verify(postLikeRepository).save(any(PostLike.class));
    }

    @Test
    void toggleLikeShouldRemoveLikeWhenAlreadyLiked() {
        PostLike existing = PostLike.builder().id(50L).post(post).user(user).build();
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findByEmail("gayantha@university.edu")).thenReturn(Optional.of(user));
        when(postLikeRepository.findByPostIdAndUserId(10L, 1L)).thenReturn(Optional.of(existing));

        boolean liked = likeService.toggleLike(10L, "gayantha@university.edu");

        assertThat(liked).isFalse();
        verify(postLikeRepository).delete(existing);
    }

    @Test
    void getLikeCountShouldReturnCountWhenPostExists() {
        when(postRepository.existsById(10L)).thenReturn(true);
        when(postLikeRepository.countByPostId(10L)).thenReturn(7L);

        assertThat(likeService.getLikeCount(10L)).isEqualTo(7L);
    }

    @Test
    void getLikeCountShouldThrowWhenPostMissing() {
        when(postRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> likeService.getLikeCount(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}