package com.blog.myblog.domain.post.repository;

import com.blog.myblog.domain.post.entity.CategoryEntity;
import com.blog.myblog.domain.post.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity,Long> {

    @Query("SELECT p FROM PostEntity p " +
            "JOIN FETCH p.user u " +
            "JOIN FETCH p.category c " +
            "ORDER BY p.createdAt DESC")
    List<PostEntity> findAllWithUserAndCategoryOrderByCreatedAtDesc();

    // 게시글별 댓글 개수를 Map으로 조회
    @Query("SELECT p.id, COUNT(c.id) FROM PostEntity p " +
            "LEFT JOIN CommentEntity c ON c.post.id = p.id " +
            "GROUP BY p.id")
    List<Object[]> findPostCommentCounts();

}
