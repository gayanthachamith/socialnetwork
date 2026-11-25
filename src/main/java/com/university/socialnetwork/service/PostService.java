package com.university.socialnetwork.service;

import com.university.socialnetwork.dto.PostDto;
import com.university.socialnetwork.dto.PostRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    PostDto createPost(String authorEmail, PostRequest request);

    Page<PostDto> getFeed(String currentUserEmail, Pageable pageable);

    Page<PostDto> getPostsByUser(Long userId, String currentUserEmail, Pageable pageable);

    PostDto getPostById(Long postId, String currentUserEmail);

    PostDto updatePost(Long postId, String currentUserEmail, PostRequest request);

    void deletePost(Long postId, String currentUserEmail);

}
