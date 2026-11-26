package com.university.socialnetwork.service;


import com.university.socialnetwork.dto.PostDto;
import com.university.socialnetwork.dto.PostRequest;
import com.university.socialnetwork.entity.Post;
import com.university.socialnetwork.entity.Role;
import com.university.socialnetwork.entity.User;
import com.university.socialnetwork.exception.ResourceNotFoundException;
import com.university.socialnetwork.exception.UnauthorizedActionException;
import com.university.socialnetwork.mapper.PostMapper;
import com.university.socialnetwork.repository.CommentRepository;
import com.university.socialnetwork.repository.PostLikeRepository;
import com.university.socialnetwork.repository.PostRepository;
import com.university.socialnetwork.repository.UserRepository;
import com.university.socialnetwork.service.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostServiceImpl postService;

    private User author;
    private User otherUser;
    private Post samplePost;
    private PostDto sampleDto;

    @BeforeEach
    void setUp() {
        author = User.builder()
                .id(1L)
                .firstName("Gayantha")
                .lastName("Chamith")
                .email("gayantha@university.edu")
                .role(Role.STUDENT)
                .build();

        otherUser = User.builder()
                .id(2L)
                .firstName("Alan")
                .lastName("Turing")
                .email("alan@university.edu")
                .role(Role.STUDENT)
                .build();

        samplePost = Post.builder()
                .id(10L)
                .author(author)
                .content("Hello campus!")
                .build();

        sampleDto = PostDto.builder()
                .id(10L)
                .content("Hello campus!")
                .build();
    }

    @Test
    void createPostShouldSaveAndReturnDto() {
        PostRequest request = new PostRequest();
        request.setContent("  Hello campus!  ");

        when(userRepository.findByEmail("gayantha@university.edu")).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenReturn(samplePost);
        when(postMapper.toDto(samplePost, 0L, 0L, false)).thenReturn(sampleDto);

        PostDto result = postService.createPost("gayantha@university.edu", request);

        assertThat(result).isEqualTo(sampleDto);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void getFeedShouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findByEmail("gayantha@university.edu")).thenReturn(Optional.of(author));
        when(postRepository.findAllByOrderByCreatedAtDesc(pageable))
                .thenReturn(new PageImpl<>(List.of(samplePost), pageable, 1));
        when(postLikeRepository.countByPostId(10L)).thenReturn(2L);
        when(commentRepository.countByPostId(10L)).thenReturn(1L);
        when(postLikeRepository.existsByPostIdAndUserId(10L, 1L)).thenReturn(true);
        when(postMapper.toDto(samplePost, 2L, 1L, true)).thenReturn(sampleDto);

        Page<PostDto> page = postService.getFeed("gayantha@university.edu", pageable);

        assertThat(page.getContent()).containsExactly(sampleDto);
    }

    @Test
    void updatePostShouldThrowWhenNotAuthor() {
        PostRequest request = new PostRequest();
        request.setContent("Updated content");

        when(userRepository.findByEmail("alan@university.edu")).thenReturn(Optional.of(otherUser));
        when(postRepository.findById(10L)).thenReturn(Optional.of(samplePost));

        assertThatThrownBy(() -> postService.updatePost(10L, "alan@university.edu", request))
                .isInstanceOf(UnauthorizedActionException.class);

        verify(postRepository, never()).save(any());
    }

    @Test
    void deletePostShouldAllowAuthor() {
        when(userRepository.findByEmail("gayantha@university.edu")).thenReturn(Optional.of(author));
        when(postRepository.findById(10L)).thenReturn(Optional.of(samplePost));

        postService.deletePost(10L, "gayantha@university.edu");

        verify(postRepository).delete(samplePost);
    }

    @Test
    void deletePostShouldThrowWhenUnauthorized() {
        when(userRepository.findByEmail("alan@university.edu")).thenReturn(Optional.of(otherUser));
        when(postRepository.findById(10L)).thenReturn(Optional.of(samplePost));

        assertThatThrownBy(() -> postService.deletePost(10L, "alan@university.edu"))
                .isInstanceOf(UnauthorizedActionException.class);

        verify(postRepository, never()).delete(any());
    }

    @Test
    void getPostByIdShouldThrowWhenMissing() {
        when(userRepository.findByEmail("gayantha@university.edu")).thenReturn(Optional.of(author));
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostById(99L, "gayantha@university.edu"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}