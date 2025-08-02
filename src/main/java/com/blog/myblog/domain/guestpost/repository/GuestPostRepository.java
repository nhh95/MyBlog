package com.blog.myblog.domain.guestpost.repository;

import com.blog.myblog.domain.post.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestPostRepository extends JpaRepository<PostEntity,Long> {
}
