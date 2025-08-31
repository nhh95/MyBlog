package com.blog.myblog.domain.comment.service;

import com.blog.myblog.domain.comment.dto.CommentRequestDTO;
import com.blog.myblog.domain.comment.dto.CommentResponseDTO;
import com.blog.myblog.domain.comment.entity.CommentEntity;
import com.blog.myblog.domain.comment.repository.CommentRepository;
import com.blog.myblog.domain.post.entity.PostEntity;
import com.blog.myblog.domain.post.repository.PostRepository;
import com.blog.myblog.domain.post.service.PostService;
import com.blog.myblog.domain.user.entity.GuestUserEntity;
import com.blog.myblog.domain.user.entity.UserEntity;
import com.blog.myblog.domain.user.repository.GuestUserRepository;
import com.blog.myblog.domain.user.repository.UserRepository;
import com.blog.myblog.domain.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final GuestUserRepository GuestUserRepository;

    public CommentResponseDTO createComment(CommentRequestDTO requestDTO){

        if(requestDTO.getIsGuest() != null && requestDTO.getIsGuest()){
            return createGuestComment(requestDTO);
        }


        UserEntity user = userRepository.findByEmail(requestDTO.getUserEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수없습니다"));

        PostEntity post = postRepository.findById(requestDTO.getPostId())
                .orElseThrow(() -> new RuntimeException("게시글을 찾을수없습니다"));

        String escapedContent = HtmlUtils.htmlEscape(requestDTO.getCommentContent());


        CommentEntity comment = new CommentEntity();
        comment.setCommentContent(escapedContent);
        comment.setUser(user);
        comment.setPost(post);
        comment.setIsGuestComment(false);

        CommentEntity savedComment = commentRepository.save(comment);

        return CommentResponseDTO.fromEntity(savedComment);
    }

    private CommentResponseDTO createGuestComment(CommentRequestDTO requestDTO){
        PostEntity post = postRepository.findById(requestDTO.getPostId()).orElseThrow(() -> new RuntimeException("게시글을 찾을수 없습니다"));

        GuestUserEntity guestUser = new GuestUserEntity();
        guestUser.setNickname(requestDTO.getGuestNickname());
        guestUser.setPassword(bCryptPasswordEncoder.encode(requestDTO.getGuestPassword()));

        GuestUserEntity savedGuestUser = GuestUserRepository.save(guestUser);

        CommentEntity comment = new CommentEntity();

        String escapedContent = HtmlUtils.htmlEscape(requestDTO.getCommentContent());
        comment.setCommentContent(escapedContent);
        comment.setGuestUser(savedGuestUser);
        comment.setPost(post);
        comment.setIsGuestComment(true);

        CommentEntity savedComment = commentRepository.save(comment);

        return CommentResponseDTO.fromEntity(savedComment);



    }

    // 댓글 조회 (특정 게시글의 모든 댓글)
    public List<CommentResponseDTO> getCommentsByPostId(Long postId) {
        return commentRepository.findAllByPostId(postId)
                .stream()
                .map(CommentResponseDTO::fromEntity)
                .toList();
    }

    // 댓글 수정
    public CommentResponseDTO updateComment(Long commentId, CommentRequestDTO requestDTO, String userEmail) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        //비회원댓글인지 확인
        if(comment.getIsGuestComment() != null && comment.getIsGuestComment()){
            return UpdateGuestComment(commentId, requestDTO);
        }

        // 회원 댓글 처리
        if (userEmail == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        if (!comment.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("댓글 수정 권한이 없습니다.");
        }

        String escapedContent = HtmlUtils.htmlEscape(requestDTO.getCommentContent());
        comment.setCommentContent(escapedContent);
        CommentEntity updatedComment = commentRepository.save(comment);
        return CommentResponseDTO.fromEntity(updatedComment);

    }

    //비회원 댓글수정
    private CommentResponseDTO UpdateGuestComment(Long commentId,CommentRequestDTO requestDTO){
        CommentEntity comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("댓글을 찾을수 없습니다"));

        if(!bCryptPasswordEncoder.matches(requestDTO.getGuestPassword(),comment.getGuestUser().getPassword())){
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }

        String escapedContent = HtmlUtils.htmlEscape(requestDTO.getCommentContent());
        comment.setCommentContent(escapedContent);
        CommentEntity updatedComment = commentRepository.save(comment);
        return CommentResponseDTO.fromEntity(updatedComment);
    }

    // 댓글 삭제
    public void deleteComment(Long commentId, String userEmail) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 비회원 댓글인지 확인
        if (comment.getIsGuestComment() != null && comment.getIsGuestComment()) {
            throw new RuntimeException("비회원 댓글은 별도 API를 사용하세요.");
        }


        if (!comment.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }

    //비회원 댓글 삭제
    public void deleteGuestComment(Long commentId,String password){
        CommentEntity comment = commentRepository.findById(commentId).orElseThrow(()-> new RuntimeException("댓글을 찾을수없습니다"));

        if(!comment.getIsGuestComment() || comment.getGuestUser() == null){
            throw new RuntimeException("비회원 댓글이 아닙니다");
        }

        if(!bCryptPasswordEncoder.matches(password,comment.getGuestUser().getPassword())){
            throw new RuntimeException("비밀번호가 일치하지 않습니다");

        }

        commentRepository.delete(comment);

    }

    // 댓글 개수 조회
    public int getCommentCount(Long postId) {
        return commentRepository.countByPostId(postId);
    }
}



