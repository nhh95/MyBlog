package com.blog.myblog.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRequestDTO {

    @NotBlank
    private String title;

    @NotBlank
    private String content;


    private String categoryName;
}
