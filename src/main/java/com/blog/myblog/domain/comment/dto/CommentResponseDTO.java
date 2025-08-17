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

    public static CommentResponseDTO fromEntity(com.blog.myblog.domain.comment.entity.CommentEntity entity) {
        return CommentResponseDTO.builder()
                .id(entity.getId())
                .commentContent(entity.getCommentContent())
                .createdAt(formatDateTime(entity.getCreatedAt()))
                .updatedAt(formatDateTime(entity.getUpdatedAt()))
                .userNickname(entity.getUser().getNickname())
                .userEmail(entity.getUser().getEmail())
                .postId(entity.getPost().getId())
                .build();
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
    }
}

