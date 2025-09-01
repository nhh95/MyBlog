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
public class GuestUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;
    private String password; // 암호화된 비밀번호

    @CreatedDate
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "guestUser", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<PostEntity> postEntityList = new ArrayList<>();

    public void addGuestPostEntity(PostEntity entity) {


        entity.setGuestUser(this);
        postEntityList.add(entity);

    }

    public void removeGuestPostEntity(PostEntity entity) {
        this.postEntityList.remove(entity);
        entity.setGuestUser(null);
    }
}