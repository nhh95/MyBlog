package com.blog.myblog.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRequestDTO {


    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 50, message = "제목은 최대 50자까지 가능합니다.")
    private String title;

    @NotBlank
    private String content;

    // 비회원용 필드 추가
    private String guestNickname;
    private String guestSecret;

    // 인증용 필드 (수정/삭제 시 사용)
    private String verificationSecret;


    private String categoryName;
}
