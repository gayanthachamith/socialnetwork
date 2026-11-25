package com.university.socialnetwork.dto;

import com.university.socialnetwork.entity.FriendRequestStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequestDto {
    private Long id;
    private String senderName;
    private String receiverName;
    private FriendRequestStatus status;
}