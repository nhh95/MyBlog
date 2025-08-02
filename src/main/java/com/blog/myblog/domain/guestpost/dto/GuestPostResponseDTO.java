package com.blog.myblog.domain.guestpost.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GuestPostResponseDTO {



        private Long id;
        private String title;
        private String content;

        private String authorName;


        private String categoryName;

        private Long viewCount;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;


}
