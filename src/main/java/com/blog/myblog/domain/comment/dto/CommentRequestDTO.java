package com.blog.myblog.domain.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequestDTO {

    private Long id;
    private String commentContent;
    private Long postId;
    private String userEmail;

    // 비회원용 필드 추가
    private String guestNickname;
    private String guestPassword;
    private Boolean isGuest = false;



}
