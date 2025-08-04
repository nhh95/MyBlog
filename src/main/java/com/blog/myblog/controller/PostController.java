package com.blog.myblog.controller;

import com.blog.myblog.domain.post.dto.PostRequestDTO;
import com.blog.myblog.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    //글 생성 페이지
    @GetMapping("/post/create-post/{categoryName}")
    public String createPage(@PathVariable String categoryName, Model model){
        PostRequestDTO dto = new PostRequestDTO();
        dto.setCategoryName(categoryName);

        model.addAttribute("postRequestDTO",dto);

        return "create-post";
    }

    //글 생성 수행
    @PostMapping("/post/create-post/{categoryName}")
    public String createProcess (@PathVariable String categoryName,@ModelAttribute PostRequestDTO dto) {
        dto.setCategoryName(categoryName);
        postService.createOnePost(dto);

        return "redirect:/post/user-free-board";
    }

    //회원 자유게시판 글 목록가져오기
    @GetMapping("/post/user-free-board")
    public String readUserFreeBoardPage(Model model) {
        model.addAttribute("boardList", postService.readAllPost());
        return "user-free-board";
    }

    //회원 자유게시판 글 하나 읽기
    @GetMapping("/post/user-free-board/{id}")
    public String readUserIdPage (@PathVariable("id") Long id,Model model) {
        model.addAttribute("POST",postService.readOnePost(id));
        return "user-free-post";

    }
    // 글 수정 페이지
    @GetMapping("/post/update-post/{id}")
    public String updatePage (@PathVariable("id") Long id, Model model) {

        if(!postService.isAccess(id)){

            return "redirect:/post/user-free-board";

        }

        model.addAttribute("POST",postService.readOnePost(id));

        return "update-post";
    }

    //글 수정 수행
    @PostMapping("/post/update-post/{id}")
    public String updateProcess(@PathVariable("id") Long id,PostRequestDTO dto){

        if(!postService.isAccess(id)){

            return "redirect:/post/user-free-board";
        }
        postService.updateOnePost(id,dto);
        return "redirect:/post/user-free-board";
    }


    //글 삭제 수행
    @PostMapping("/post/delete-post/{id}")
    public String deleteProcess(@PathVariable("id") Long id){

        if (!postService.isAccess(id)) {
            return "redirect:/post/user-free-board";
        }
        postService.deleteOnePost(id);
        return "redirect:/post/user-free-board";
    }

}
