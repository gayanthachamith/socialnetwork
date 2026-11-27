package com.university.socialnetwork.controller;

import com.university.socialnetwork.config.SecurityConfig;
import com.university.socialnetwork.dto.CommentDto;
import com.university.socialnetwork.dto.CommentRequest;
import com.university.socialnetwork.dto.FriendRequestDto;
import com.university.socialnetwork.dto.PostDto;
import com.university.socialnetwork.dto.PostRequest;
import com.university.socialnetwork.entity.FriendRequestStatus;
import com.university.socialnetwork.service.FriendRequestService;
import com.university.socialnetwork.service.impl.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = WebPageController.class)
@Import(SecurityConfig.class)
class WebPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserServiceImpl userService;

    @MockitoBean
    private PostServiceImpl postService;

    @MockitoBean(name = "commentService")
    private CommentServiceImpl commentService;

    @MockitoBean
    private PostLikeServiceImpl postLikeService;

    @MockitoBean
    private FriendRequestServiceImpl friendRequestService;

    @Test
    void shouldRenderLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void shouldRenderRegisterPage() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    @WithMockUser(username = "student@university.edu", roles = "STUDENT")
    void shouldRenderFeedPageWithPostsAndRequests() throws Exception {
        PostDto post = PostDto.builder()
                .id(1L)
                .content("Welcome to campus")
                .authorName("Alex Student")
                .authorId(10L)
                .createdAt(Instant.now())
                .likeCount(2L)
                .commentCount(1L)
                .likedByCurrentUser(false)
                .build();

        Page<PostDto> page = new PageImpl<>(List.of(post), PageRequest.of(0, 10), 1);
        FriendRequestDto req = FriendRequestDto.builder()
                .id(5L)
                .senderName("Jordan Peer")
                .receiverName("Alex Student")
                .status(FriendRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(postService.getFeed(eq("student@university.edu"), any(Pageable.class))).thenReturn(page);
        when(friendRequestService.getPendingRequests("student@university.edu")).thenReturn(List.of(req));
        when(commentService.getCommentsByPost(1L)).thenReturn(List.of(
                CommentDto.builder()
                        .id(9L)
                        .content("Great post")
                        .authorName("Sam Peer")
                        .createdAt(Instant.now())
                        .build()
        ));

        mockMvc.perform(get("/feed"))
                .andExpect(status().isOk())
                .andExpect(view().name("feed"))
                .andExpect(model().attributeExists("posts", "postPage", "incomingRequests"));
    }

    @Test
    @WithMockUser(username = "student@university.edu", roles = "STUDENT")
    void shouldCreatePostAndRedirectToFeed() throws Exception {
        mockMvc.perform(post("/feed/posts")
                        .param("content", "Hello university feed")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feed"));

        verify(postService).createPost(eq("student@university.edu"), any(PostRequest.class));
    }

    @Test
    @WithMockUser(username = "student@university.edu", roles = "STUDENT")
    void shouldToggleLikeAndRedirectToFeed() throws Exception {
        mockMvc.perform(post("/feed/posts/1/like")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feed"));

        verify(postLikeService, atLeastOnce()).toggleLike(any(), any());
    }

    @Test
    @WithMockUser(username = "student@university.edu", roles = "STUDENT")
    void shouldAddCommentAndRedirectToFeed() throws Exception {
        mockMvc.perform(post("/feed/posts/1/comments")
                        .param("content", "Nice update!")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feed"));

        verify(commentService).addComment(eq(1L), eq("student@university.edu"), any(CommentRequest.class));
    }

    @Test
    @WithMockUser(username = "student@university.edu", roles = "STUDENT")
    void shouldSendFriendRequestAndRedirectToFeed() throws Exception {
        mockMvc.perform(post("/feed/friends/request")
                        .param("receiverId", "42")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feed"));

        verify(friendRequestService).sendRequest("student@university.edu", 42L);
    }

    @Test
    @WithMockUser(username = "student@university.edu", roles = "STUDENT")
    void shouldAcceptFriendRequestAndRedirectToFeed() throws Exception {
        mockMvc.perform(post("/feed/friends/5/respond")
                        .param("decision", "ACCEPTED")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feed"));

        verify(friendRequestService).acceptRequest(5L, "student@university.edu");
    }

    @Test
    @WithMockUser(username = "student@university.edu", roles = "STUDENT")
    void shouldDeclineFriendRequestAndRedirectToFeed() throws Exception {
        mockMvc.perform(post("/feed/friends/5/respond")
                        .param("decision", "REJECTED")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feed"));

        verify(friendRequestService).rejectRequest(5L, "student@university.edu");
    }
}