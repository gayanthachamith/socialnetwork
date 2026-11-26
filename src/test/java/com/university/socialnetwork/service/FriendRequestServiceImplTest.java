package com.university.socialnetwork.service;

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
import com.university.socialnetwork.service.impl.FriendRequestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendRequestServiceImplTest {

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private FriendRequestServiceImpl friendRequestService;

    private User sender;
    private User receiver;
    private FriendRequest pendingRequest;

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .id(1L)
                .email("alice@university.edu")
                .firstName("Alice")
                .lastName("Smith")
                .build();

        receiver = User.builder()
                .id(2L)
                .email("bob@university.edu")
                .firstName("Bob")
                .lastName("Jones")
                .build();

        pendingRequest = FriendRequest.builder()
                .id(10L)
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .build();
    }

    @Test
    void sendRequest_shouldCreatePendingRequest_whenValid() {
        when(userRepository.findByEmail("alice@university.edu")).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(friendRequestRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(Optional.empty());
        when(friendRequestRepository.findBySenderIdAndReceiverId(2L, 1L)).thenReturn(Optional.empty());
        when(friendRequestRepository.save(any(FriendRequest.class))).thenAnswer(invocation -> {
            FriendRequest req = invocation.getArgument(0);
            req.setId(10L);
            return req;
        });

        FriendRequestDto result = friendRequestService.sendRequest("alice@university.edu", 2L);

        assertThat(result).isNotNull();
        verify(friendRequestRepository).save(any(FriendRequest.class));
    }

    @Test
    void sendRequest_shouldThrowIllegalArgumentException_whenSendingToSelf() {
        when(userRepository.findByEmail("alice@university.edu")).thenReturn(Optional.of(sender));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));

        assertThatThrownBy(() -> friendRequestService.sendRequest("alice@university.edu", 1L))
                .isInstanceOf(IllegalArgumentException.class);

        verify(friendRequestRepository, never()).save(any());
    }

    @Test
    void sendRequest_shouldThrowIllegalStateException_whenRequestAlreadyExists() {
        when(userRepository.findByEmail("alice@university.edu")).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(friendRequestRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(Optional.of(pendingRequest));

        assertThatThrownBy(() -> friendRequestService.sendRequest("alice@university.edu", 2L))
                .isInstanceOf(IllegalStateException.class);

        verify(friendRequestRepository, never()).save(any());
    }

    @Test
    void sendRequest_shouldThrowIllegalStateException_whenReverseRequestExists() {
        FriendRequest reverseRequest = FriendRequest.builder()
                .id(11L)
                .sender(receiver)
                .receiver(sender)
                .status(FriendRequestStatus.PENDING)
                .build();

        when(userRepository.findByEmail("alice@university.edu")).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(friendRequestRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(Optional.empty());
        when(friendRequestRepository.findBySenderIdAndReceiverId(2L, 1L)).thenReturn(Optional.of(reverseRequest));

        assertThatThrownBy(() -> friendRequestService.sendRequest("alice@university.edu", 2L))
                .isInstanceOf(IllegalStateException.class);

        verify(friendRequestRepository, never()).save(any());
    }

    @Test
    void sendRequest_shouldThrowResourceNotFoundException_whenReceiverMissing() {
        when(userRepository.findByEmail("alice@university.edu")).thenReturn(Optional.of(sender));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendRequestService.sendRequest("alice@university.edu", 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void acceptRequest_shouldUpdateStatusToAccepted_whenReceiverIsCurrentUser() {
        when(friendRequestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));
        when(userRepository.findByEmail("bob@university.edu")).thenReturn(Optional.of(receiver));
        when(friendRequestRepository.save(any(FriendRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FriendRequestDto result = friendRequestService.acceptRequest(10L, "bob@university.edu");

        assertThat(result).isNotNull();
        assertThat(pendingRequest.getStatus()).isEqualTo(FriendRequestStatus.ACCEPTED);
        verify(friendRequestRepository).save(pendingRequest);
    }

    @Test
    void acceptRequest_shouldThrowUnauthorizedActionException_whenCallerIsNotReceiver() {
        when(friendRequestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));
        when(userRepository.findByEmail("alice@university.edu")).thenReturn(Optional.of(sender));

        assertThatThrownBy(() -> friendRequestService.acceptRequest(10L, "alice@university.edu"))
                .isInstanceOf(UnauthorizedActionException.class);

        verify(friendRequestRepository, never()).save(any());
    }

    @Test
    void acceptRequest_shouldThrowIllegalStateException_whenRequestNotPending() {
        pendingRequest.setStatus(FriendRequestStatus.ACCEPTED);
        when(friendRequestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));
        when(userRepository.findByEmail("bob@university.edu")).thenReturn(Optional.of(receiver));

        assertThatThrownBy(() -> friendRequestService.acceptRequest(10L, "bob@university.edu"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectRequest_shouldUpdateStatusToRejected_whenReceiverIsCurrentUser() {
        when(friendRequestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));
        when(userRepository.findByEmail("bob@university.edu")).thenReturn(Optional.of(receiver));
        when(friendRequestRepository.save(any(FriendRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FriendRequestDto result = friendRequestService.rejectRequest(10L, "bob@university.edu");

        assertThat(result).isNotNull();
        assertThat(pendingRequest.getStatus()).isEqualTo(FriendRequestStatus.REJECTED);
        verify(friendRequestRepository).save(pendingRequest);
    }

    @Test
    void rejectRequest_shouldThrowUnauthorizedActionException_whenCallerIsNotReceiver() {
        when(friendRequestRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));
        when(userRepository.findByEmail("alice@university.edu")).thenReturn(Optional.of(sender));

        assertThatThrownBy(() -> friendRequestService.rejectRequest(10L, "alice@university.edu"))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void getPendingRequests_shouldReturnPendingRequestsForUser() {
        when(userRepository.findByEmail("bob@university.edu")).thenReturn(Optional.of(receiver));
        when(friendRequestRepository.findByReceiverIdAndStatus(2L, FriendRequestStatus.PENDING))
                .thenReturn(List.of(pendingRequest));

        List<FriendRequestDto> results = friendRequestService.getPendingRequests("bob@university.edu");

        assertThat(results).hasSize(1);
    }

    @Test
    void getFriends_shouldReturnMappedFriendDtosFromAcceptedRequests() {
        FriendRequest acceptedAsSender = FriendRequest.builder()
                .id(20L)
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.ACCEPTED)
                .build();

        User thirdUser = User.builder()
                .id(3L)
                .email("carol@university.edu")
                .firstName("Carol")
                .lastName("Lee")
                .build();

        FriendRequest acceptedAsReceiver = FriendRequest.builder()
                .id(21L)
                .sender(thirdUser)
                .receiver(sender)
                .status(FriendRequestStatus.ACCEPTED)
                .build();

        UserDto bobDto = UserDto.builder().id(2L).email("bob@university.edu").build();
        UserDto carolDto = UserDto.builder().id(3L).email("carol@university.edu").build();

        when(userRepository.findByEmail("alice@university.edu")).thenReturn(Optional.of(sender));
        when(friendRequestRepository.findBySenderIdAndStatusOrReceiverIdAndStatus(
                1L, FriendRequestStatus.ACCEPTED, 1L, FriendRequestStatus.ACCEPTED))
                .thenReturn(List.of(acceptedAsSender, acceptedAsReceiver));
        when(userMapper.toDto(receiver)).thenReturn(bobDto);
        when(userMapper.toDto(thirdUser)).thenReturn(carolDto);

        List<UserDto> friends = friendRequestService.getFriends("alice@university.edu");

        assertThat(friends).containsExactly(bobDto, carolDto);
    }
}