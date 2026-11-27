package com.university.socialnetwork.service.impl;

import com.university.socialnetwork.entity.Post;
import com.university.socialnetwork.entity.PostLike;
import com.university.socialnetwork.entity.User;
import com.university.socialnetwork.repository.PostLikeRepository;
import com.university.socialnetwork.repository.PostRepository;
import com.university.socialnetwork.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeServiceImpl {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public long countLikes(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException("Post not found with id: " + postId);
        }
        return postLikeRepository.countByPostId(postId);
    }

    public boolean isLikedByUser(Long postId, String callerEmail) {
        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException("Post not found with id: " + postId);
        }
        User user = findUserByEmail(callerEmail);
        return postLikeRepository.existsByPostIdAndUserId(postId, user.getId());
    }

    @Transactional
    public boolean toggleLike(String callerEmail, Long postId) {
        User user = findUserByEmail(callerEmail);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));

        Optional<PostLike> existing = postLikeRepository.findByPostIdAndUserId(postId, user.getId());
        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            return false;
        }

        PostLike like = PostLike.builder()
                .post(post)
                .user(user)
                .build();
        postLikeRepository.save(like);
        return true;
    }

    private User findUserByEmail(String email) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return userRepository.findByEmail(normalized)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + normalized));
    }
}