package com.blog.myblog.domain.post.repository;

import com.blog.myblog.domain.post.entity.CategoryEntity;
import com.blog.myblog.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity,Long> {

  CategoryEntity findByCategoryName(String categoryName );
}
