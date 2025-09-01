package com.blog.myblog.domain.comment.repository;

import com.blog.myblog.domain.comment.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<CommentEntity,Long> {

    Optional<CommentEntity> findByPostIdAndId(Long postId,Long Id);

    List<CommentEntity> findAllByPostId(Long postId);

    int countByPostId(Long postId);
}
