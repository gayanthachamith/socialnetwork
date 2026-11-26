package com.university.socialnetwork.controller;


import com.university.socialnetwork.dto.PostDto;
import com.university.socialnetwork.dto.PostRequest;
import com.university.socialnetwork.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import java.security.Principal;

@RestController
@RequestMapping("api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody PostRequest request, Principal principal){
        PostDto created = postService.createPost(principal.getName(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    @GetMapping
    public ResponseEntity<Page<PostDto>> getFeed(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Principal principal) {
        String currentUserEmail = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(postService.getFeed(currentUserEmail, pageable));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPostById(
            @PathVariable Long postId,
            Principal principal) {
        String currentUserEmail = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(postService.getPostById(postId, currentUserEmail));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostRequest request,
            Principal principal) {
        return ResponseEntity.ok(postService.updatePost(postId, principal.getName(), request));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            Principal principal) {
        postService.deletePost(postId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
