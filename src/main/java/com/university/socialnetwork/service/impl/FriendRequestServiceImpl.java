package com.university.socialnetwork.service.impl;

import com.university.socialnetwork.dto.FriendRequestDto;
import com.university.socialnetwork.dto.UserDto;
import com.university.socialnetwork.entity.FriendRequest;
import com.university.socialnetwork.entity.FriendRequestStatus;
import com.university.socialnetwork.entity.User;
import com.university.socialnetwork.exception.ResourceNotFoundException;
import com.university.socialnetwork.exception.UnauthorizedActionException;
import com.university.socialnetwork.mapper.UserMapper;
import com.university.socialnetwork.repository.FriendRequestRepository;
import com.university.socialnetwork.repository.UserRepository;
import com.university.socialnetwork.service.FriendRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendRequestServiceImpl implements FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public FriendRequestDto sendRequest(String senderEmail, Long receiverId) {
        User sender = findUserByEmail(senderEmail);
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + receiverId));

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("You cannot send a friend request to yourself");
        }

        if (friendRequestRepository.findBySenderIdAndReceiverId(sender.getId(), receiver.getId()).isPresent()
                || friendRequestRepository.findBySenderIdAndReceiverId(receiver.getId(), sender.getId()).isPresent()) {
            throw new IllegalStateException("A friend request already exists between these users");
        }

        FriendRequest request = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .build();

        FriendRequest saved = friendRequestRepository.save(request);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public FriendRequestDto acceptRequest(Long requestId, String currentUserEmail) {
        FriendRequest request = findRequestOrThrow(requestId);
        User currentUser = findUserByEmail(currentUserEmail);

        if (!request.getReceiver().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("Only the receiver can accept this friend request");
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending friend requests can be accepted");
        }

        request.setStatus(FriendRequestStatus.ACCEPTED);
        FriendRequest updated = friendRequestRepository.save(request);
        return mapToDto(updated);
    }

    @Override
    @Transactional
    public FriendRequestDto rejectRequest(Long requestId, String currentUserEmail) {
        FriendRequest request = findRequestOrThrow(requestId);
        User currentUser = findUserByEmail(currentUserEmail);

        if (!request.getReceiver().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("Only the receiver can reject this friend request");
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending friend requests can be rejected");
        }

        request.setStatus(FriendRequestStatus.REJECTED);
        FriendRequest updated = friendRequestRepository.save(request);
        return mapToDto(updated);
    }

    @Override
    public List<FriendRequestDto> getPendingRequests(String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);
        return friendRequestRepository.findByReceiverIdAndStatus(currentUser.getId(), FriendRequestStatus.PENDING)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<UserDto> getFriends(String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);
        Long userId = currentUser.getId();

        List<FriendRequest> acceptedRequests = friendRequestRepository
                .findBySenderIdAndStatusOrReceiverIdAndStatus(
                        userId, FriendRequestStatus.ACCEPTED,
                        userId, FriendRequestStatus.ACCEPTED
                );

        return acceptedRequests.stream()
                .map(req -> req.getSender().getId().equals(userId) ? req.getReceiver() : req.getSender())
                .map(userMapper::toDto)
                .toList();
    }

    private FriendRequestDto mapToDto(FriendRequest request) {
        User sender = request.getSender();
        User receiver = request.getReceiver();

        LocalDateTime createdAt = request.getCreatedAt() != null
                ? LocalDateTime.ofInstant(request.getCreatedAt(), ZoneOffset.UTC)
                : null;

        return FriendRequestDto.builder()
                .id(request.getId())
                .senderName(formatUserName(sender))
                .receiverName(formatUserName(receiver))
                .status(request.getStatus())
                .createdAt(createdAt)
                .build();
    }

    private String formatUserName(User user) {
        if (user == null) {
            return null;
        }
        String first = user.getFirstName() != null ? user.getFirstName() : "";
        String last = user.getLastName() != null ? user.getLastName() : "";
        String combined = (first + " " + last).trim();
        return combined.isEmpty() ? user.getEmail() : combined;
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private FriendRequest findRequestOrThrow(Long requestId) {
        return friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found with id: " + requestId));
    }
}