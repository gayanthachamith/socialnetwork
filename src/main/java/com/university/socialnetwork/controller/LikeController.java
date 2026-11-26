package com.university.socialnetwork.controller;

import com.university.socialnetwork.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
@RequiredArgsConstructor
public class LikeController{
    private final LikeService likeService;

    @PostMapping("/toggle")
    public ResponseEntity toggleLike(
            @PathVariable Long postId,
            Principal principal) {
        boolean liked = likeService.toggleLike(postId, principal.getName());
        long count = likeService.getLikeCount(postId);
        return ResponseEntity.ok(Map.of(
                "postId", postId,
                "liked", liked,
                "likeCount", count
        ));
    }

    @GetMapping("/count")
    public ResponseEntity getLikeCount(@PathVariable Long postId) {
        return ResponseEntity.ok(Map.of("likeCount", likeService.getLikeCount(postId)));
    }
}