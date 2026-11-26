package com.university.socialnetwork.controller;

import com.university.socialnetwork.dto.FriendRequestDto;
import com.university.socialnetwork.dto.UserDto;
import com.university.socialnetwork.service.FriendRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    @PostMapping("/requests/{receiverId}")
    public ResponseEntity<FriendRequestDto> sendRequest(
            @PathVariable Long receiverId,
            Principal principal) {
        FriendRequestDto created = friendRequestService.sendRequest(principal.getName(), receiverId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<FriendRequestDto> acceptRequest(
            @PathVariable Long requestId,
            Principal principal) {
        return ResponseEntity.ok(friendRequestService.acceptRequest(requestId, principal.getName()));
    }

    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<FriendRequestDto> rejectRequest(
            @PathVariable Long requestId,
            Principal principal) {
        return ResponseEntity.ok(friendRequestService.rejectRequest(requestId, principal.getName()));
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<List<FriendRequestDto>> getPendingRequests(Principal principal) {
        return ResponseEntity.ok(friendRequestService.getPendingRequests(principal.getName()));
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getFriends(Principal principal) {
        return ResponseEntity.ok(friendRequestService.getFriends(principal.getName()));
    }
}