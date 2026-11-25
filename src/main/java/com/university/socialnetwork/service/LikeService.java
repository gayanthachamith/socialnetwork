package com.university.socialnetwork.service;

public interface LikeService {

    /**
     * Toggles a like on a post for the given user.
     *
     * @return true if the post is now liked, false if unliked
     */
    boolean toggleLike(Long postId, String userEmail);

    long getLikeCount(Long postId);

    boolean isLikedByUser(Long postId, String userEmail);
}