package com.blog.myblog.controller;

import com.blog.myblog.domain.comment.dto.CommentRequestDTO;
import com.blog.myblog.domain.comment.dto.CommentResponseDTO;
import com.blog.myblog.domain.comment.entity.CommentEntity;
import com.blog.myblog.domain.comment.repository.CommentRepository;
import com.blog.myblog.domain.comment.service.CommentService;
import com.blog.myblog.domain.post.entity.PostEntity;
import com.blog.myblog.domain.post.repository.PostRepository;
import com.blog.myblog.domain.user.entity.UserEntity;
import com.blog.myblog.domain.user.repository.UserRepository;
import com.blog.myblog.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")

@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping

    public ResponseEntity<Map<String, Object>> createComment(@RequestBody CommentRequestDTO requestDTO) {
        try {
            CommentResponseDTO comment = commentService.createComment(requestDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("comment", comment);
            response.put("message", "댓글이 성공적으로 저장되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    // 특정 게시글의 댓글 조회
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByPost(@PathVariable Long postId) {
        List<CommentResponseDTO> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }


    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequestDTO requestDTO,
            Authentication authentication) {
        try {
            String userEmail = (authentication != null) ? authentication.getName() : null;
            CommentResponseDTO comment = commentService.updateComment(commentId, requestDTO, userEmail);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("comment", comment);
            response.put("message", "댓글이 성공적으로 수정되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            commentService.deleteComment(commentId, userEmail);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "댓글이 성공적으로 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 댓글 개수 조회
    @GetMapping("/count/{postId}")
    public ResponseEntity<Map<String, Object>> getCommentCount(@PathVariable Long postId) {
        int count = commentService.getCommentCount(postId);
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    //비회원 댓글 삭제
    @DeleteMapping("/guest/{commentId}")
    public  ResponseEntity<Map<String, Object>> deleteGuestComment(@PathVariable Long commentId,@RequestBody Map<String,String> requestData){
        try{
            String password = requestData.get("password");
            commentService.deleteGuestComment(commentId,password);
            Map<String,Object> response = new HashMap<>();
            response.put("success",true);
            response.put("message","댓글이 성공적으로 삭제되었습니다");
            return ResponseEntity.ok(response);
        }catch(Exception e){
            Map<String,Object> response = new HashMap<>();
            response.put("success",false);
            response.put("message",e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
