package com.blog.myblog.domain.post.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostResponseDTO {



        private Long id;
        private String title;
        private String content;
        private String userNickname;
        private String categoryName;
        private Long viewCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;


}
