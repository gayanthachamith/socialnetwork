package com.university.socialnetwork.service.impl;

import com.university.socialnetwork.entity.Post;
import com.university.socialnetwork.entity.PostLike;
import com.university.socialnetwork.entity.User;
import com.university.socialnetwork.exception.ResourceNotFoundException;
import com.university.socialnetwork.repository.PostLikeRepository;
import com.university.socialnetwork.repository.PostRepository;
import com.university.socialnetwork.repository.UserRepository;
import com.university.socialnetwork.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeServiceImpl implements LikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public boolean toggleLike(Long postId, String userEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndUserId(postId, user.getId());

        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            return false;
        }

        PostLike newLike = PostLike.builder()
                .post(post)
                .user(user)
                .build();
        postLikeRepository.save(newLike);
        return true;
    }

    @Override
    public long getLikeCount(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }
        return postLikeRepository.countByPostId(postId);
    }

    @Override
    public boolean isLikedByUser(Long postId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        return postLikeRepository.existsByPostIdAndUserId(postId, user.getId());
    }
}