package com.blog.myblog.domain.comment.service;

import com.blog.myblog.domain.comment.dto.CommentRequestDTO;
import com.blog.myblog.domain.comment.dto.CommentResponseDTO;
import com.blog.myblog.domain.comment.entity.CommentEntity;
import com.blog.myblog.domain.comment.repository.CommentRepository;
import com.blog.myblog.domain.post.entity.PostEntity;
import com.blog.myblog.domain.post.repository.PostRepository;
import com.blog.myblog.domain.post.service.PostService;
import com.blog.myblog.domain.user.entity.UserEntity;
import com.blog.myblog.domain.user.repository.UserRepository;
import com.blog.myblog.domain.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public CommentResponseDTO createComment(CommentRequestDTO requestDTO){
        UserEntity user = userRepository.findByEmail(requestDTO.getUserEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수없습니다"));

        PostEntity post = postRepository.findById(requestDTO.getPostId())
                .orElseThrow(() -> new RuntimeException("게시글을 찾을수없습니다"));

        CommentEntity comment = new CommentEntity();
        comment.setCommentContent(requestDTO.getCommentContent());
        comment.setUser(user);
        comment.setPost(post);

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

        if (!comment.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("댓글 수정 권한이 없습니다.");
        }

        comment.setCommentContent(requestDTO.getCommentContent());
        CommentEntity updatedComment = commentRepository.save(comment);
        return CommentResponseDTO.fromEntity(updatedComment);
    }

    // 댓글 삭제
    public void deleteComment(Long commentId, String userEmail) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }

    // 댓글 개수 조회
    public int getCommentCount(Long postId) {
        return commentRepository.countByPostId(postId);
    }
}



