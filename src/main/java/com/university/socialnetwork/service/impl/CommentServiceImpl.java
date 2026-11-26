package com.university.socialnetwork.service.impl;

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
import com.university.socialnetwork.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto addComment(Long postId, String authorEmail, CommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + authorEmail));

        Comment comment = Comment.builder()
                .post(post)
                .author(author)
                .content(request.getContent().trim())
                .build();

        Comment saved = commentRepository.save(comment);
        return commentMapper.toDto(saved);
    }

    @Override
    public List<CommentDto> getCommentsByPost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(commentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String currentUserEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        boolean isCommentAuthor = comment.getAuthor().getId().equals(currentUser.getId());
        boolean isPostAuthor = comment.getPost().getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isCommentAuthor && !isPostAuthor && !isAdmin) {
            throw new UnauthorizedActionException("You are not allowed to delete this comment");
        }

        commentRepository.delete(comment);
    }
}