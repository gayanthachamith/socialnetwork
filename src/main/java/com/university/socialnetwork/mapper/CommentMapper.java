package com.university.socialnetwork.mapper;

import com.university.socialnetwork.dto.CommentDto;
import com.university.socialnetwork.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentDto toDto(Comment comment) {
        String authorName = comment.getAuthor().getFirstName() + " " + comment.getAuthor().getLastName();
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorName(authorName)
                .createdAt(comment.getCreatedAt())
                .build();
    }
}