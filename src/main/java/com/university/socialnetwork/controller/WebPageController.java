package com.university.socialnetwork.controller;

import com.university.socialnetwork.dto.PostDto;
import com.university.socialnetwork.dto.PostRequest;
import com.university.socialnetwork.dto.RegisterRequest;
import com.university.socialnetwork.entity.FriendRequestStatus;
import com.university.socialnetwork.service.impl.CommentServiceImpl;
import com.university.socialnetwork.service.impl.FriendRequestServiceImpl;
import com.university.socialnetwork.service.impl.PostLikeServiceImpl;
import com.university.socialnetwork.service.impl.UserServiceImpl;
import com.university.socialnetwork.service.impl.PostServiceImpl;
import com.university.socialnetwork.dto.CommentRequest;
import com.university.socialnetwork.service.impl.CommentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebPageController {

    private final UserServiceImpl userService;
    private final PostServiceImpl postService;
    private final CommentServiceImpl commentService;
    private final PostLikeServiceImpl postLikeService;
    private final FriendRequestServiceImpl friendRequestService;

    @GetMapping("/")
    public String home(Principal principal) {
        return principal != null ? "redirect:/feed" : "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(@RequestParam String firstName,
                                 @RequestParam String lastName,
                                 @RequestParam String email,
                                 @RequestParam String password,
                                 @RequestParam String faculty,
                                 @RequestParam Integer studyYear,
                                 @RequestParam(required = false) String bio,
                                 RedirectAttributes redirectAttributes) {
        try {
            RegisterRequest request = new RegisterRequest();
            request.setFirstName(firstName);
            request.setLastName(lastName);
            request.setEmail(email);
            request.setPassword(password);
            request.setFaculty(faculty);
            request.setStudyYear(studyYear);

            userService.register(request);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful. Please sign in.");
            return "redirect:/login";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/feed")
    public String feedPage(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           Model model,
                           Principal principal) {
        String email = principal.getName();
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        Page<PostDto> postPage = postService.getFeed(email, pageable);
        List<PostDto> posts = postPage.getContent();

        model.addAttribute("currentUserEmail", email);
        model.addAttribute("postPage", postPage);
        model.addAttribute("posts", posts);
        model.addAttribute("commentService", commentService);
        model.addAttribute("incomingRequests", friendRequestService.getPendingRequests(email));
        return "feed";
    }

    @PostMapping("/feed/posts")
    public String createPost(@RequestParam String content, Principal principal) {
        PostRequest request = new PostRequest();
        request.setContent(content);
        postService.createPost(principal.getName(), request);
        return "redirect:/feed";
    }

    @PostMapping("/feed/posts/{postId}/delete")
    public String deletePost(@PathVariable Long postId, Principal principal) {
        postService.deletePost(postId, principal.getName());
        return "redirect:/feed";
    }

    @PostMapping("/feed/posts/{postId}/like")
    public String toggleLike(@PathVariable Long postId, Principal principal) {
        postLikeService.toggleLike(principal.getName(), postId);
        return "redirect:/feed";
    }

    @PostMapping("/feed/posts/{postId}/comments")
    public String addComment(@PathVariable Long postId,
                             @RequestParam String content,
                             Principal principal) {
        CommentRequest request = new CommentRequest();
        request.setContent(content);
        commentService.addComment(postId, principal.getName(), request);
        return "redirect:/feed";
    }

    @PostMapping("/feed/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId, Principal principal) {
        commentService.deleteComment(commentId, principal.getName());
        return "redirect:/feed";
    }

    @PostMapping("/feed/friends/request")
    public String sendFriendRequest(@RequestParam Long receiverId,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        try {
            friendRequestService.sendRequest(principal.getName(), receiverId);
            redirectAttributes.addFlashAttribute("successMessage", "Friend request sent.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/feed";
    }

    @PostMapping("/feed/friends/{requestId}/respond")
    public String respondFriendRequest(@PathVariable Long requestId,
                                       @RequestParam FriendRequestStatus decision,
                                       Principal principal) {
        String callerEmail = principal.getName();
        if (decision == FriendRequestStatus.ACCEPTED) {
            friendRequestService.acceptRequest(requestId, callerEmail);
        } else if (decision == FriendRequestStatus.REJECTED) {
            friendRequestService.rejectRequest(requestId, callerEmail);
        } else {
            throw new IllegalArgumentException("Unsupported friend request decision: " + decision);
        }
        return "redirect:/feed";
    }
}