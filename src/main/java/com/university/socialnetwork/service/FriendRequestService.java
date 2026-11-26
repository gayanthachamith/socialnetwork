package com.university.socialnetwork.service;

import com.university.socialnetwork.dto.FriendRequestDto;
import com.university.socialnetwork.dto.UserDto;

import java.util.List;

public interface FriendRequestService {

    FriendRequestDto sendRequest(String senderEmail, Long receiverId);
    FriendRequestDto acceptRequest(Long requestId, String currentUserEmail);
    FriendRequestDto rejectRequest(Long requestID, String currentUserEmail);
    List<FriendRequestDto> getPendingRequests(String currentUserEmail);
    List<UserDto> getFriends(String currentUserEmail);

}
