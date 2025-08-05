package com.blog.myblog.controller;

import com.blog.myblog.domain.post.dto.PostRequestDTO;
import com.blog.myblog.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.Set;

@Controller
@SessionAttributes("viewedPosts")
@RequiredArgsConstructor
public class PostController {

    @ModelAttribute("viewedPosts")
    public Set<Long> viewedPosts() {
        return new HashSet<>();
    }

    private final PostService postService;

    //글 생성 페이지
    @GetMapping("/post/createPost/{categoryName}")
    public String createPage(@PathVariable String categoryName, Model model){
        PostRequestDTO dto = new PostRequestDTO();
        dto.setCategoryName(categoryName);
        model.addAttribute("postRequestDTO",dto);

        return "createPost";
    }

    //글 생성 수행
    @PostMapping("/post/createPost/{categoryName}")
    public String createProcess (@PathVariable String categoryName,@ModelAttribute PostRequestDTO dto) {
        dto.setCategoryName(categoryName);
        postService.createOnePost(dto);

        return "redirect:/post/userFreeBoard";
    }

    //회원 자유게시판 글 목록가져오기
    @GetMapping("/post/userFreeBoard")
    public String readUserFreeBoardPage(Model model) {
        model.addAttribute("boardList", postService.readAllPost());

        return "userFreeBoard";
    }

    //회원 자유게시판 글 하나 읽기
    @GetMapping("/post/userFreeBoard/{id}")
    public String readUserIdPage (@PathVariable("id") Long id,Model model,@ModelAttribute("viewedPosts") Set<Long> viewedPosts) {

        model.addAttribute("POST",postService.readOnePost(id));

        return "userFreePost";
    }

    // 글 수정 페이지
    @GetMapping("/post/updatePost/{id}")
    public String updatePage (@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {

        if(!postService.isAccess(id)){
            redirectAttributes.addFlashAttribute("msg","글 수정 권한이 없습니다");
            return "redirect:/post/userFreeBoard";
        }

        model.addAttribute("POST",postService.readOnePost(id));

        return "updatePost";
    }

    //글 수정 수행
    @PostMapping("/post/updatePost/{id}")
    public String updateProcess(@PathVariable("id") Long id,PostRequestDTO dto){

        if(!postService.isAccess(id)){

            return "redirect:/post/userFreeBoard";
        }

        postService.updateOnePost(id,dto);

        return "redirect:/post/userFreeBoard";
    }


    //글 삭제 수행
    @PostMapping("/post/deletePost/{id}")
    public String deleteProcess(@PathVariable("id") Long id,RedirectAttributes redirectAttributes){

        if(!postService.isAccess(id)){
            redirectAttributes.addFlashAttribute("msg","글 삭제 권한이 없습니다");
            return "redirect:/post/userFreeBoard";

        }

        postService.deleteOnePost(id);

        return "redirect:/post/userFreeBoard";
    }


}
