package com.university.socialnetwork.mapper;

import com.university.socialnetwork.dto.PostDto;
import com.university.socialnetwork.entity.Post;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {
    public PostDto toDto(Post post, long likeCount, long commentCount, boolean likedByCurrentUser){
        String authorName = post.getAuthor().getFirstName()+ " "+ post.getAuthor().getLastName();
        return PostDto.builder()
                .id(post.getId())
                .content(post.getContent())
                .authorName(authorName)
                .authorId(post.getAuthor().getId())
                .createdAt(post.getCreatedAt())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .likedByCurrentUser(likedByCurrentUser)
                .build();

    }
}
