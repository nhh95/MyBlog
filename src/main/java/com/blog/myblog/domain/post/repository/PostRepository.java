package com.blog.myblog.domain.post.repository;

import com.blog.myblog.domain.post.entity.CategoryEntity;
import com.blog.myblog.domain.post.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity,Long> {

    @Query("SELECT p FROM PostEntity p JOIN FETCH p.user u JOIN FETCH p.category c")
    List<PostEntity> findAllWithUserAndCategory();

}
