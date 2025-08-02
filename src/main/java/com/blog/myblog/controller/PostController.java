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

@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    //글 생성 페이지
    @GetMapping("/post/create/{categoryName}")
    public String createPage(@PathVariable String categoryName, Model model){
        PostRequestDTO dto = new PostRequestDTO();
        dto.setCategoryName(categoryName);

        model.addAttribute("postRequestDTO",dto);

        return "createPost";
    }

    //글 생성 수행
    @PostMapping("/post/create/{categoryName}")
    public String createProcess (@PathVariable String categoryName,@ModelAttribute PostRequestDTO dto) {
        dto.setCategoryName(categoryName);
        postService.createOnePost(dto);

        return "redirect:/post/read";
    }


    @GetMapping("/post/user-free-board")
    public String readPage(Model model) {
        model.addAttribute("boardList", postService.readAllPost());
        return "UserFreeBoard";
    }

}
