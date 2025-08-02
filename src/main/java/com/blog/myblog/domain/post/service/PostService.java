package com.blog.myblog.domain.post.service;

import com.blog.myblog.domain.post.dto.PostRequestDTO;
import com.blog.myblog.domain.post.dto.PostResponseDTO;
import com.blog.myblog.domain.post.entity.CategoryEntity;
import com.blog.myblog.domain.post.entity.PostEntity;
import com.blog.myblog.domain.post.repository.CategoryRepository;
import com.blog.myblog.domain.post.repository.PostRepository;
import com.blog.myblog.domain.user.entity.UserEntity;
import com.blog.myblog.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    //게시글 하나 만들기
    @Transactional
    public void createOnePost(PostRequestDTO dto){

        PostEntity postEntity = new PostEntity();

        postEntity.setTitle(dto.getTitle());
        postEntity.setContent(dto.getContent());

        CategoryEntity category = categoryRepository.findByCategoryName(dto.getCategoryName());
        postEntity.setCategory(category);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow();
        userEntity.addPostEntity(postEntity);

        userRepository.save(userEntity);
    }

    //게시글 하나 읽기
    @Transactional(readOnly = true)
    public PostResponseDTO readOnePost(Long id) {
        PostEntity postEntity = postRepository.findById(id).orElseThrow();

        PostResponseDTO dto = new PostResponseDTO();

        postEntity.setViewCount(postEntity.getViewCount() + 1);

        dto.setId(postEntity.getId());
        dto.setTitle(postEntity.getTitle());
        dto.setContent(postEntity.getContent());
        dto.setUserNickname(postEntity.getUser().getNickname());
        dto.setCategoryName(postEntity.getCategory().getCategoryName());
        dto.setViewCount(postEntity.getViewCount());
        dto.setCreatedAt(postEntity.getCreatedAt());
        dto.setUpdatedAt(postEntity.getUpdatedAt());

        return dto;


    }

    //게시글 전부 읽기
    @Transactional
    public List<PostResponseDTO> readAllPost() {

        List<PostEntity> list = postRepository.findAllWithUserAndCategory();

        List<PostResponseDTO> dtos = new ArrayList<>();

        for(PostEntity postEntity : list) {
            PostResponseDTO dto = new PostResponseDTO();

            dto.setId(postEntity.getId());
            dto.setTitle(postEntity.getTitle());
            dto.setContent(postEntity.getContent());
            dto.setUserNickname(postEntity.getUser().getNickname());
            dto.setCategoryName(postEntity.getCategory().getCategoryName());
            dto.setViewCount(postEntity.getViewCount());
            dto.setCreatedAt(postEntity.getCreatedAt());
            dto.setUpdatedAt(postEntity.getUpdatedAt());

            dtos.add(dto);

        }
        return dtos;
    }

    @Transactional
    public void updateOnePost(Long id,PostRequestDTO dto){

        PostEntity postEntity = postRepository.findById(id).orElseThrow();

        postEntity.setTitle(dto.getTitle());
        postEntity.setContent(dto.getContent());

        postRepository.save(postEntity);
    }

    @Transactional
    public void deleteOnePost(Long id) {
        postRepository.deleteById(id);
    }

    public Boolean isAccess(Long id) {
        //현재 로그인 되어있는 유저의 Email
        String sessionEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        //현재 로그인 되어 있는 유저의 ROLE
        String sessionRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority();

        //어드민 이면 접근 가능
        if("ROLE_ADMIN".equals(sessionRole)) {
            return true;
        }

        //게시글 id에 대해 본인이 작성했는지 확인
        String postUserEmail = postRepository.findById(id).orElseThrow().getUser().getEmail();
        if(sessionEmail.equals((postUserEmail))){

            return true;

        }

        //나머지는 불가
        return false;
    }
}
