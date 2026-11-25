package com.university.socialnetwork.service;


import com.university.socialnetwork.dto.CommentDto;
import com.university.socialnetwork.dto.CommentRequest;
import com.university.socialnetwork.entity.Comment;
import com.university.socialnetwork.entity.Post;
import com.university.socialnetwork.entity.Role;
import com.university.socialnetwork.entity.User;
import com.university.socialnetwork.exception.ResourceNotFoundException;
import com.university.socialnetwork.exception.UnauthorizedActionException;
import com.university.socialnetwork.mapper.CommentMapper;
import com.university.socialnetwork.repository.CommentRepository;
import com.university.socialnetwork.repository.PostRepository;
import com.university.socialnetwork.repository.UserRepository;
import com.university.socialnetwork.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User postAuthor;
    private User commenter;
    private User stranger;
    private Post post;
    private Comment comment;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        postAuthor = User.builder()
                .id(1L)
                .firstName("Gayantha")
                .lastName("Chamith")
                .email("gayantha@university.edu")
                .role(Role.STUDENT)
                .build();

        commenter = User.builder()
                .id(2L)
                .firstName("Grace")
                .lastName("Hopper")
                .email("grace@university.edu")
                .role(Role.STUDENT)
                .build();

        stranger = User.builder()
                .id(3L)
                .firstName("Alan")
                .lastName("Turing")
                .email("alan@university.edu")
                .role(Role.STUDENT)
                .build();

        post = Post.builder()
                .id(10L)
                .author(postAuthor)
                .content("Post content")
                .build();

        comment = Comment.builder()
                .id(100L)
                .post(post)
                .author(commenter)
                .content("Nice post!")
                .build();

        commentDto = CommentDto.builder()
                .id(100L)
                .content("Nice post!")
                .build();
    }

    @Test
    void addCommentShouldSaveAndReturnDto() {
        CommentRequest request = new CommentRequest();
        request.setContent("  Nice post!  ");

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findByEmail("grace@university.edu")).thenReturn(Optional.of(commenter));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toDto(comment)).thenReturn(commentDto);

        CommentDto result = commentService.addComment(10L, "grace@university.edu", request);

        assertThat(result).isEqualTo(commentDto);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void getCommentsByPostShouldReturnOrderedList() {
        when(postRepository.existsById(10L)).thenReturn(true);
        when(commentRepository.findByPostIdOrderByCreatedAtAsc(10L)).thenReturn(List.of(comment));
        when(commentMapper.toDto(comment)).thenReturn(commentDto);

        List<CommentDto> results = commentService.getCommentsByPost(10L);

        assertThat(results).containsExactly(commentDto);
    }

    @Test
    void deleteCommentShouldAllowPostAuthor() {
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));
        when(userRepository.findByEmail("gayantha@university.edu")).thenReturn(Optional.of(postAuthor));

        commentService.deleteComment(100L, "gayantha@university.edu");

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteCommentShouldThrowWhenNeitherAuthorNorAdmin() {
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));
        when(userRepository.findByEmail("alan@university.edu")).thenReturn(Optional.of(stranger));

        assertThatThrownBy(() -> commentService.deleteComment(100L, "alan@university.edu"))
                .isInstanceOf(UnauthorizedActionException.class);

        verify(commentRepository, never()).delete(any());
    }

    @Test
    void getCommentsByPostShouldThrowWhenPostMissing() {
        when(postRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> commentService.getCommentsByPost(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}