package com.blog.myblog.domain.post.service;

import com.blog.myblog.domain.post.dto.PostRequestDTO;
import com.blog.myblog.domain.post.dto.PostResponseDTO;
import com.blog.myblog.domain.post.entity.CategoryEntity;
import com.blog.myblog.domain.post.entity.PostEntity;
import com.blog.myblog.domain.post.repository.CategoryRepository;
import com.blog.myblog.domain.post.repository.PostRepository;
import com.blog.myblog.domain.user.entity.UserEntity;
import com.blog.myblog.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;


import javax.lang.model.element.NestingKind;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static java.nio.file.Files.exists;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ViewedPostsHolder viewedPostsHolder;

    @Value("${file.temp.path}")
    private String tempFilePath;

    @Value("${file.final.path}")
    private String finalFilePath;

    //게시글 하나 만들기
    @Transactional
    public void createOnePost(PostRequestDTO dto){

        // 이미지 임시 폴더에서 최종 폴더로 이동 및 경로 업데이트
        String processedContent = savePostImagesAndGetContent(dto.getContent());

        PostEntity postEntity = new PostEntity();
        postEntity.setTitle(dto.getTitle());
        postEntity.setContent(processedContent);

        CategoryEntity category = categoryRepository.findByCategoryName(dto.getCategoryName());
        postEntity.setCategory(category);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow();
        postEntity.setUser(userEntity);

        userEntity.addPostEntity(postEntity);
        userRepository.save(userEntity);
    }



    //게시글 하나 읽기
    @Transactional
    public PostResponseDTO readOnePost(Long id) {

        //  세션에 기록이 없으면 조회수 증가
        if (!viewedPostsHolder.contains(id)) {
            PostEntity entityForCount = postRepository.findById(id).orElseThrow();
            entityForCount.setViewCount(entityForCount.getViewCount() + 1);

            viewedPostsHolder.add(id);
        }

        PostEntity postEntity = postRepository.findById(id).orElseThrow();

        PostResponseDTO dto = new PostResponseDTO();

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

        System.out.println("▶️ readAllPost size = " + list.size());

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

        String oldContent = postEntity.getContent();
        String newContent = dto.getContent();

        // 기존 게시글에서 삭제된 이미지 파일 처리
        cleanupDeletedImages(postEntity.getContent(), dto.getContent());

        // 새로운 이미지 파일 저장 및 경로 업데이트
        String updatedContent = savePostImagesAndGetContent(dto.getContent());

        postEntity.setTitle(dto.getTitle());
        postEntity.setContent(updatedContent);

        postRepository.save(postEntity);
    }

    @Transactional
    public void deleteOnePost(Long id) {

        // 1. 게시글 내용을 조회
        PostEntity postEntity = postRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("해당 게시글이 존재하지 않습니다. id=" + id));

        // 2. 게시글 본문에서 이미지 URL 추출 및 파일 삭제
        deletePostImages(postEntity.getContent());

        // 3. 데이터베이스에서 게시글 삭제
        postRepository.delete(postEntity);
    }

    @Transactional(readOnly = true)
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

        //나중에 비회원 게시판 만들때  비회원이 글 작성할때 사용한 패스워드를 통해서 인증 가능하게 만들어야될듯
        //글을 수정과 삭제 할려면 작성시에 입력한 패스워드 입력

        //나머지는 불가
        return false;
    }


    // ----- 이미지 파일 처리 관련 private 메서드들 -----

    // 게시글 내용에서 이미지 URL들을 추출
    private Set<String> extractImageUrls(String content) {
        Set<String> imageUrls = new HashSet<>();
        Pattern pattern = Pattern.compile("<img[^>]*src=[\"']([^\"']*)[\"'][^>]*>");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            imageUrls.add(matcher.group(1));
        }
        return imageUrls;
    }

    // 임시 폴더의 이미지를 최종 폴더로 이동시키고 게시글 내용을 반환
    private String savePostImagesAndGetContent(String content){
        String updatedContent = content;
        Set<String> imageUrls = extractImageUrls(content);

        for (String imageUrl : imageUrls) {
            if (imageUrl.contains("/summernoteImage/")) {
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

                File tempFile = new File(tempFilePath + fileName);
                File finalFile = new File(finalFilePath + fileName);

                // 이미 최종 폴더에 있는 이미지인지 확인하여 불필요한 이동 방지
                if (!finalFile.exists() && tempFile.exists()) {
                    try {
                        Files.move(tempFile.toPath(), finalFile.toPath());
                        updatedContent = updatedContent.replace(imageUrl, "/summernoteImage/" + fileName);
                    } catch (IOException e) {
                        System.err.println("Failed to move image file: " + fileName);
                        e.printStackTrace();
                    }
                }
            }
        }
        return updatedContent;
    }

    // 수정 시 삭제된 이미지 파일들을 정리
    private void cleanupDeletedImages(String oldContent, String newContent){
        Set<String> oldImageUrls = extractImageUrls(oldContent);
        Set<String> newImageUrls = extractImageUrls(newContent);

        // oldImageUrls에서 newImageUrls에 포함된 URL을 제거
        oldImageUrls.removeAll(newImageUrls);

        for (String imageUrl : oldImageUrls) {
            try {
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                File imageFile = new File(finalFilePath + fileName);

                if (imageFile.exists()) {
                    Files.delete(imageFile.toPath());
                    System.out.println("Deleted old image file: " + fileName);
                }
            } catch (Exception e) {
                System.err.println("Failed to delete old image file: " + imageUrl);
            }
        }
    }

    // 게시글 삭제 시 이미지 파일들을 정리
    private void deletePostImages(String content) {
        Set<String> imageUrls = extractImageUrls(content);

        for (String imageUrl : imageUrls) {
            if (imageUrl.contains("/summernoteImage/")) {
                try {
                    String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                    File imageFile = new File(finalFilePath + fileName);

                    if (imageFile.exists()) {
                        Files.delete(imageFile.toPath());
                        System.out.println("Deleted image file: " + fileName);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to delete image file: " + imageUrl);
                }
            }
        }
    }
}



