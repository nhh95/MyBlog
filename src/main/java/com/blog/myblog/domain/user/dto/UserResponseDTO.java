package com.blog.myblog.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {


    private String email;
    private String nickname;
    private String createdAt;
    private String role;


}
