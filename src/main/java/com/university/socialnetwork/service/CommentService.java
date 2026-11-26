package com.university.socialnetwork.service;

import com.university.socialnetwork.dto.CommentDto;
import com.university.socialnetwork.dto.CommentRequest;

import java.util.List;

public interface CommentService {

    CommentDto addComment(Long postId, String authorEmail, CommentRequest request);

    List<CommentDto> getCommentsByPost(Long postId);

    void deleteComment(Long commentId, String currentUserEmail);
}