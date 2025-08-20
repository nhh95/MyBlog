package com.blog.myblog.controller;

import com.blog.myblog.domain.post.dto.PostResponseDTO;
import com.blog.myblog.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class MainController {

    private final PostService postService;

    @GetMapping("/")
    public String mainPage(Model model) {
        // 포트폴리오 카테고리의 첫 번째 게시물 가져오기
        Pageable pageable = PageRequest.of(0, 1, Sort.by("id").descending());
        Page<PostResponseDTO> portfolioPosts = postService.findAllByCategory("portfolioboard", pageable);

        if (!portfolioPosts.isEmpty()) {
            PostResponseDTO portfolioPost = portfolioPosts.getContent().get(0);
            model.addAttribute("portfolioPost", portfolioPost);
        }

        return "main";
    }


}
