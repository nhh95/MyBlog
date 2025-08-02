package com.blog.myblog.domain.guestpost.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuestPostRequestDTO {

    @NotBlank
    private String title;

    @NotBlank
    private String content;


    private String guestName;
    private String guestPassword;

    private String categoryName;
}
