package com.blog.myblog.domain.post.service;

import com.blog.myblog.domain.comment.entity.CommentEntity;
import com.blog.myblog.domain.comment.repository.CommentRepository;
import com.blog.myblog.domain.file.MinioService;
import com.blog.myblog.domain.post.dto.PostRequestDTO;
import com.blog.myblog.domain.post.dto.PostResponseDTO;
import com.blog.myblog.domain.post.entity.CategoryEntity;
import com.blog.myblog.domain.post.entity.PostEntity;
import com.blog.myblog.domain.post.repository.CategoryRepository;
import com.blog.myblog.domain.post.repository.PostRepository;
import com.blog.myblog.domain.user.entity.GuestUserEntity;
import com.blog.myblog.domain.user.entity.UserEntity;
import com.blog.myblog.domain.user.repository.GuestUserRepository;
import com.blog.myblog.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;




import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CommentRepository commentRepository;
    private final GuestUserRepository guestUserRepository; // 추가
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(); // 초기화

    private final ViewedPostsHolder viewedPostsHolder;
    private final MinioService minioService;

    @Value("${file.temp.path}")
    private String tempFilePath;



    //게시글 하나 만들기
    @Transactional
    public void createOnePost(PostRequestDTO dto){


        // 이미지 임시 폴더에서 최종 폴더로 이동 및 경로 업데이트
        String processedContent = savePostImagesAndGetContent(dto.getContent());

        PostEntity postEntity = new PostEntity();
        postEntity.setTitle(dto.getTitle());
        postEntity.setContent(processedContent);

        CategoryEntity category = categoryRepository.findByCategoryName(dto.getCategoryName());
        if (category == null) {

            throw new RuntimeException("카테고리를 찾을 수 없습니다: " + dto.getCategoryName());
        }
        postEntity.setCategory(category);

        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("authentication: " + authentication);

        // 비회원 글 작성인지 확인
        if ("anonymousUser".equals(authentication) && dto.getGuestNickname() != null && dto.getGuestSecret() != null) {

            // 비회원 처리
            GuestUserEntity guestUser = new GuestUserEntity();
            guestUser.setNickname(dto.getGuestNickname());
            guestUser.setPassword(bCryptPasswordEncoder.encode(dto.getGuestSecret()));

            GuestUserEntity savedGuestUser = guestUserRepository.save(guestUser);


            postEntity.setGuestUser(savedGuestUser);
            savedGuestUser.addGuestPostEntity(postEntity);
        } else {

            // 기존 회원 처리
            UserEntity userEntity = userRepository.findByEmail(authentication).orElseThrow();
            postEntity.setUser(userEntity);
            userEntity.addPostEntity(postEntity);
        }

        // 비회원, 회원 모두 게시글 저장 필요

        PostEntity savedPost = postRepository.save(postEntity);

    }





    //게시글 하나 읽기
    @Transactional
    public PostResponseDTO readOnePost(Long id) {

        PostEntity postEntity = postRepository.findById(id).orElseThrow();
        //  세션에 기록이 없으면 조회수 증가
        if (!viewedPostsHolder.contains(id)) {
            postEntity.setViewCount(postEntity.getViewCount() + 1);
            viewedPostsHolder.add(id);
        }


        PostResponseDTO dto = new PostResponseDTO();

        dto.setId(postEntity.getId());
        dto.setTitle(postEntity.getTitle());
        dto.setContent(postEntity.getContent());

        // 작성자 닉네임 처리 (회원/비회원 구분)
        if (postEntity.getUser() != null) {
            dto.setUserNickname(postEntity.getUser().getNickname());
        } else if (postEntity.getGuestUser() != null) {
            dto.setUserNickname(postEntity.getGuestUser().getNickname() + " (비회원)");
        }


        dto.setCategoryName(postEntity.getCategory().getCategoryName());
        dto.setViewCount(postEntity.getViewCount());
        dto.setCreatedAt(postEntity.getCreatedAt());
        dto.setUpdatedAt(postEntity.getUpdatedAt());

        return dto;


    }


    /*게시글 목록 가져오기*/
    public Page<PostResponseDTO> findAllByCategory(String categoryName, Pageable pageable) {
        Page<PostEntity> postPage = postRepository.findByCategoryNameOrderByIdDesc(categoryName,pageable);

        // 댓글 개수 조회
        List<Object[]> commentCounts = postRepository.findPostCommentCounts();
        Map<Long, Long> commentCountMap = commentCounts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));


        return postPage.map(post ->{
            PostResponseDTO dto = new PostResponseDTO();

            dto.setId(post.getId());
            dto.setTitle(post.getTitle());
            dto.setContent(post.getContent());

            // 작성자 닉네임 처리 (회원/비회원 구분) - 수정된 부분
            if (post.getUser() != null) {
                dto.setUserNickname(post.getUser().getNickname());
            } else if (post.getGuestUser() != null) {
                dto.setUserNickname(post.getGuestUser().getNickname() + " (비회원)");
            }


            dto.setCategoryName(post.getCategory().getCategoryName());
            dto.setViewCount(post.getViewCount());
            dto.setCreatedAt(post.getCreatedAt());
            dto.setUpdatedAt(post.getUpdatedAt());
            dto.setFirstImageUrl(extractFirstImageUrl(post.getContent()));
            dto.setPlainContent(getPlainTextContent(post.getContent()));
            // 댓글 개수 설정
            Long commentCount = commentCountMap.getOrDefault(post.getId(), 0L);
            dto.setCommentCount(commentCount.intValue());

            return dto;
        });
    }

    @Transactional
    public void updateOnePost(Long id,PostRequestDTO dto){

        PostEntity postEntity = postRepository.findById(id).orElseThrow();

        // 비회원 게시글인 경우 비밀번호 검증
        if (postEntity.getGuestUser() != null) {
            if (dto.getVerificationSecret() == null ||
                    !bCryptPasswordEncoder.matches(dto.getVerificationSecret(), postEntity.getGuestUser().getPassword())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
        }

        String oldContent = postEntity.getContent();
        String newContent = dto.getContent();

        // 기존 게시글에서 삭제된 이미지 파일 처리
        cleanupDeletedImages(oldContent, newContent);

        // 새로운 이미지 파일 저장 및 경로 업데이트
        String updatedContent = savePostImagesAndGetContent(newContent);

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

    // 비회원 게시글 삭제를 위한 새로운 메소드
    @Transactional
    public void deleteGuestPost(Long id, String password) {
        PostEntity postEntity = postRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("해당 게시글이 존재하지 않습니다. id=" + id));

        // 비회원 게시글인지 확인하고 비밀번호 검증
        if (postEntity.getGuestUser() != null) {
            if (password == null || !bCryptPasswordEncoder.matches(password, postEntity.getGuestUser().getPassword())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
        } else {
            throw new IllegalArgumentException("비회원 게시글이 아닙니다.");
        }

        // 이미지 파일 삭제
        deletePostImages(postEntity.getContent());

        // 데이터베이스에서 게시글 삭제
        postRepository.delete(postEntity);
    }


    @Transactional
    public void forceDeleteGuestPost(Long id) {
        PostEntity postEntity = postRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("해당 게시글이 존재하지 않습니다. id=" + id));


        deletePostImages( postEntity.getContent());
        postRepository.delete( postEntity);
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
        PostEntity postEntity = postRepository.findById(id).orElseThrow();

        // 비회원 게시글인 경우 - 비밀번호 확인이 필요하므로 별도 처리
        if (postEntity.getGuestUser() != null) {
            return false; // 컨트롤러에서 별도 비밀번호 확인 로직 필요
        }

        //게시글 id에 대해 본인이 작성했는지 확인 (회원만)
        if (postEntity.getUser() != null) {
            String postUserEmail = postEntity.getUser().getEmail();
            return sessionEmail.equals(postUserEmail);
        }

        //나머지는 불가
        return false;
    }


    // 비회원 게시글 접근 권한 확인 메소드
    @Transactional(readOnly = true)
    public Boolean isGuestAccess(Long id, String password) {
        PostEntity postEntity = postRepository.findById(id).orElseThrow();

        if (postEntity.getGuestUser() != null && password != null) {
            return bCryptPasswordEncoder.matches(password, postEntity.getGuestUser().getPassword());
        }

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

    // 게시글 내용에서 첫 번째 이미지 URL 추출
    private String extractFirstImageUrl(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        Pattern pattern = Pattern.compile("<img[^>]*src=[\"']([^\"']*)[\"'][^>]*>");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    // HTML 태그를 제거한 순수 텍스트 내용을 반환하는 메서드
    public String getPlainTextContent(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        // HTML 태그 제거
        String plainText = content.replaceAll("<[^>]*>", "");
        // 연속된 공백을 하나로 변환
        plainText = plainText.replaceAll("\\s+", " ");
        // 앞뒤 공백 제거
        return plainText.trim();
    }


    // 임시 폴더의 이미지를 최종 폴더로 이동시키고 게시글 내용을 반환
    private String savePostImagesAndGetContent(String content){
        String updatedContent = content;
        Set<String> imageUrls = extractImageUrls(content);

        for (String imageUrl : imageUrls) {
            if (imageUrl.contains("/summernoteImage/")) {
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

                File tempFile = new File(tempFilePath + fileName);

                if (tempFile.exists()) {
                    try {
                        String minioFileUrl = minioService.minioUploadFile(tempFile);
                        updatedContent = updatedContent.replace(imageUrl, minioFileUrl);
                        Files.delete(tempFile.toPath());
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
            // MinIO에 있는 이미지 파일만 삭제 대상으로 지정
            if (imageUrl.contains("/summernoteImage/")) {
                try {
                    String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

                    // 로컬 파일 대신 MinIOService를 통해 파일 삭제
                    minioService.minioDeleteFile(fileName);
                    System.out.println("Deleted old image file from MinIO: " + fileName);
                } catch (Exception e) {
                    System.err.println("Failed to delete old image file from MinIO: " + imageUrl);
                    e.printStackTrace();
                }
            }
        }
    }

    // 게시글 삭제 시 이미지 파일들을 정리
    private void deletePostImages(String content) {
        Set<String> imageUrls = extractImageUrls(content);

        for (String imageUrl : imageUrls) {
            // MinIO에 있는 이미지 파일만 삭제 대상으로 지정
            if (imageUrl.contains("/summernoteImage/")) {
                try {
                    String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

                    // 로컬 파일 대신 MinIOService를 통해 파일 삭제
                    minioService.minioDeleteFile(fileName);
                    System.out.println("Deleted image file from MinIO: " + fileName);
                } catch (Exception e) {
                    System.err.println("Failed to delete image file from MinIO: " + imageUrl);
                    e.printStackTrace();
                }
            }
        }
    }

    // 수정 전용 권한 체크 메서드 추가
    @Transactional(readOnly = true)
    public Boolean isUpdateAccess(Long id) {
        //현재 로그인 되어있는 유저의 Email
        String sessionEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        PostEntity postEntity = postRepository.findById(id).orElseThrow();

        // 비회원 게시글인 경우 - 비밀번호 확인이 필요하므로 별도 처리
        if (postEntity.getGuestUser() != null) {
            return false; // 컨트롤러에서 별도 비밀번호 확인 로직 필요
        }

        //게시글 id에 대해 본인이 작성했는지 확인 (회원만) - ADMIN도 본인 글만 수정 가능
        if (postEntity.getUser() != null) {
            String postUserEmail = postEntity.getUser().getEmail();
            return sessionEmail.equals(postUserEmail);
        }

        //나머지는 불가
        return false;
    }

    @Transactional(readOnly = true)
    public Boolean isGuestPost(Long id) {
        PostEntity postEntity = postRepository.findById(id).orElseThrow();
        return postEntity.getGuestUser() != null; // guest_user_id가 존재하면 비회원 글
    }


}












