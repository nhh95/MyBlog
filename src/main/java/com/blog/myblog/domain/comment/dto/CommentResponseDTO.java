package com.blog.myblog.domain.comment.dto;


import com.blog.myblog.domain.comment.entity.CommentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDTO {
    private Long id;
    private String commentContent;
    private String createdAt;
    private String updatedAt;
    private String userNickname;
    private String userEmail;
    private Long postId;
    private Boolean isGuestComment;

    public static CommentResponseDTO fromEntity(CommentEntity entity) {
        String nickname;
        String email;
        Boolean isGuest = false;

        if (entity.getIsGuestComment() != null && entity.getIsGuestComment() && entity.getGuestUser() != null) {
            nickname = entity.getGuestUser().getNickname() + " (비회원)";
            email = "";
            isGuest = true;
        } else if (entity.getUser() != null) {
            nickname = entity.getUser().getNickname();
            email = entity.getUser().getEmail();
        } else {
            nickname = "알 수 없음";
            email = "";
        }



        return CommentResponseDTO.builder()
                .id(entity.getId())
                .commentContent(entity.getCommentContent())
                .createdAt(formatDateTime(entity.getCreatedAt()))
                .updatedAt(formatDateTime(entity.getUpdatedAt()))
                .userNickname(nickname)
                .userEmail(email)
                .postId(entity.getPost().getId())
                .isGuestComment(isGuest)
                .build();
    }


    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
    }
}

