package com.blog.myblog.domain.user.entity;


import com.blog.myblog.domain.post.entity.PostEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;
    private String nickname;

    @CreatedDate
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    UserRoleType role;


    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<PostEntity> PostEntityList = new ArrayList<>();

    // 유저에 대해 새로운 글을 추가할 때 : 추가할 글을 받아서 연관관계에 매핑해줌
    public void addPostEntity(PostEntity entity) {
        entity.setUserEntity(this);
        PostEntityList.add(entity);
    }

    // 유저에 대해 기존 글을 삭제할 때 : 삭제할 글을 받아서 연관관계에서 뺌
    public void removePostEntity(PostEntity entity) {
        entity.setUserEntity(null);
        PostEntityList.remove(entity);
    }

}
