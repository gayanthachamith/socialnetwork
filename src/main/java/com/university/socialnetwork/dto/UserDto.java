package com.university.socialnetwork.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String fullName;
    private String email;
    private String faculty;
    private Integer studyYear;
    private String bio;
}