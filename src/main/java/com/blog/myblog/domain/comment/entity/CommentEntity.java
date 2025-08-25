package com.blog.myblog.domain.comment.entity;


import com.blog.myblog.domain.post.entity.PostEntity;
import com.blog.myblog.domain.user.entity.GuestUserEntity;
import com.blog.myblog.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "LONGTEXT",nullable = false,length = 1000)
    private String commentContent;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id",nullable = false)
    private PostEntity post;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id",nullable = true)
    private UserEntity user;

    // 비회원 댓글 지원 필드 추가
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "guest_user_id", nullable = true)
    private GuestUserEntity guestUser;

    @Column
    private Boolean isGuestComment = false;


}
