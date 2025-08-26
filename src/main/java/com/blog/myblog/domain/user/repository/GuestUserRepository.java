package com.blog.myblog.domain.user.repository;

import com.blog.myblog.domain.user.entity.GuestUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestUserRepository extends JpaRepository<GuestUserEntity, Long> {
    Optional<GuestUserEntity> findByNickname(String nickname);
}
