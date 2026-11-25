package com.university.socialnetwork.service.impl;

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
import com.university.socialnetwork.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final PostMapper postMapper;

    @Override
    @Transactional
    public PostDto createPost(String authorEmail, PostRequest request) {
        User author = findUserByEmail(authorEmail);

        Post post = Post.builder()
                .author(author)
                .content(request.getContent().trim())
                .build();

        Post saved = postRepository.save(post);
        return postMapper.toDto(saved, 0L, 0L, false);
    }

    @Override
    public Page<PostDto> getFeed(String currentUserEmail, Pageable pageable) {
        User currentUser = findUserByEmail(currentUserEmail);
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(post -> mapPostToDto(post, currentUser.getId()));
    }

    @Override
    public Page<PostDto> getPostsByUser(Long userId, String currentUserEmail, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        User currentUser = findUserByEmail(currentUserEmail);
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(userId, pageable)
                .map(post -> mapPostToDto(post, currentUser.getId()));
    }

    @Override
    public PostDto getPostById(Long postId, String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);
        Post post = findPostOrThrow(postId);
        return mapPostToDto(post, currentUser.getId());
    }

    @Override
    @Transactional
    public PostDto updatePost(Long postId, String currentUserEmail, PostRequest request) {
        User currentUser = findUserByEmail(currentUserEmail);
        Post post = findPostOrThrow(postId);

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("Only the post author can edit this post");
        }

        post.setContent(request.getContent().trim());
        Post updated = postRepository.save(post);
        return mapPostToDto(updated, currentUser.getId());
    }

    @Override
    @Transactional
    public void deletePost(Long postId, String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);
        Post post = findPostOrThrow(postId);

        boolean isAuthor = post.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedActionException("You are not allowed to delete this post");
        }

        postRepository.delete(post);
    }

    private PostDto mapPostToDto(Post post, Long currentUserId) {
        long likeCount = postLikeRepository.countByPostId(post.getId());
        long commentCount = commentRepository.countByPostId(post.getId());
        boolean likedByCurrentUser = postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);
        return postMapper.toDto(post, likeCount, commentCount, likedByCurrentUser);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private Post findPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
    }
}