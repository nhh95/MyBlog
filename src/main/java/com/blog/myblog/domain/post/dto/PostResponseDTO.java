package com.blog.myblog.domain.post.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        private int commentCount;

        private String firstImageUrl;
        private String plainContent;


}


