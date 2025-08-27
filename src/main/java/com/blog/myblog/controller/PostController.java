package com.blog.myblog.controller;

import com.blog.myblog.domain.post.dto.PostRequestDTO;
import com.blog.myblog.domain.post.dto.PostResponseDTO;
import com.blog.myblog.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @GetMapping("/post/{categoryName}/createpost")
    public String createPage(@PathVariable String categoryName, Model model,RedirectAttributes redirectAttributes){
        PostRequestDTO dto = new PostRequestDTO();
        dto.setCategoryName(categoryName);
        model.addAttribute("postRequestDTO",dto);
        model.addAttribute("categoryName", categoryName);

        String sessionEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // 비회원 게시판 (guestfreeboard)인 경우 비회원도 글 작성 가능
        if(sessionEmail.equals("anonymousUser") && !"guestfreeboard".equals(categoryName)){
            redirectAttributes.addFlashAttribute("msg","글 생성 권한이 없습니다");
            return "redirect:/post/{categoryName}";
        }

        // 비회원인지 회원인지 구분하여 모델에 추가
        model.addAttribute("isGuest", sessionEmail.equals("anonymousUser"));

        return "createpost";
    }


    //글 생성 수행
    @PostMapping("/post/{categoryName}/createpost")
    public String createProcess (@PathVariable String categoryName,@ModelAttribute PostRequestDTO dto) {
        dto.setCategoryName(categoryName);

        try {
            postService.createOnePost(dto);
            return "redirect:/post/" + categoryName;
        } catch (Exception e) {
            // 에러 발생 시 처리
            return "redirect:/post/" + categoryName + "/createpost?error=" + e.getMessage();
        }
    }


    //회원 자유게시판 글 목록가져오기
    @GetMapping("/post/{categoryName}")
    public String readUserFreeBoardPage(@PathVariable String categoryName,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size, Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<PostResponseDTO> postPage = postService.findAllByCategory(categoryName, pageable);


        if ("portfolioboard".equals(categoryName) && postPage.getTotalElements() == 1 && !postPage.getContent().isEmpty()) {
            PostResponseDTO singlePost = postPage.getContent().get(0);
            return "redirect:/post/" + categoryName + "/" + singlePost.getId();
        }


        // 페이지네이션 계산 (5개씩 표시)
        int pageGroupSize = 5; // 한 번에 보여질 페이지 번호 개수
        int currentPageGroup = page / pageGroupSize;
        int startPage = currentPageGroup * pageGroupSize;
        int endPage = Math.min(startPage + pageGroupSize - 1, postPage.getTotalPages() - 1);

        // 페이지가 없는 경우 처리
        if (startPage < 0) startPage = 0;
        if (endPage < startPage) endPage = startPage;


        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        model.addAttribute("boardList", postPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("totalElements", postPage.getTotalElements());
        model.addAttribute("hasNext", postPage.hasNext());
        model.addAttribute("hasPrevious", postPage.hasPrevious());

        // 페이지 그룹 네비게이션을 위한 추가 변수들
        model.addAttribute("hasPreviousGroup", startPage > 0);
        model.addAttribute("hasNextGroup", endPage < postPage.getTotalPages() - 1);




        return categoryName;
    }

    //회원 자유게시판 글 하나 읽기
    @GetMapping("/post/{categoryName}/{id}")
    public String readUserIdPage (@PathVariable("categoryName")String categoryName, @PathVariable("id") Long id,Model model,@ModelAttribute("viewedPosts") Set<Long> viewedPosts) {

        model.addAttribute("POST",postService.readOnePost(id));
        model.addAttribute("categoryName", categoryName);

        // 현재 로그인 상태 확인
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isGuest = "anonymousUser".equals(authentication);
        model.addAttribute("isCurrentUserGuest", isGuest);


        // 카테고리별로 다른 템플릿 반환
        return switch (categoryName) {
            case "portfolioboard" -> "portfoliopost";
            case "guestfreeboard" -> "guestfreepost";
            case "projectboard" -> "projectpost";
            default -> "userfreepost";
        };

    }

    // 글 수정 페이지
    @GetMapping("/post/{categoryName}/updatepost/{id}")
    public String updatePage (@PathVariable("categoryName")String categoryName,@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {

        PostResponseDTO post = postService.readOnePost(id);

        // 비회원 게시글인지 확인
        boolean isGuestPost = post.getUserNickname() != null && post.getUserNickname().contains("(비회원)");

        if (isGuestPost) {
            // 비회원 게시글인 경우 비밀번호 입력 페이지로
            model.addAttribute("POST", post);
            model.addAttribute("categoryName", categoryName);
            model.addAttribute("postId", id);
            return "guestpasswordverify";
        } else {
            // 회원 게시글인 경우 기존 로직
            if(!postService.isAccess(id)){
                redirectAttributes.addFlashAttribute("msg","글 수정 권한이 없습니다");
                return "redirect:/post/" + categoryName;
            }

            model.addAttribute("POST", post);
            model.addAttribute("categoryName", categoryName);
            return "updatepost";
        }
    }


    // 비회원 게시글 비밀번호 확인 후 수정 페이지
    @PostMapping("/post/{categoryName}/updatepost/{id}/verify")
    public String verifyGuestPassword(@PathVariable("categoryName")String categoryName,
                                      @PathVariable("id") Long id,
                                      @RequestParam("password") String password,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {

        if (postService.isGuestAccess(id, password)) {
            PostResponseDTO post = postService.readOnePost(id);
            model.addAttribute("POST", post);
            model.addAttribute("categoryName", categoryName);

            return "updatepost";
        } else {
            redirectAttributes.addFlashAttribute("msg", "비밀번호가 일치하지 않습니다.");
            redirectAttributes.addAttribute("categoryName", categoryName);
            redirectAttributes.addAttribute("postId", id);
            return "redirect:/post/" + categoryName + "/updatepost/" + id;

        }
    }


    //글 수정 수행
    @PostMapping("/post/{categoryName}/updatepost/{id}")
    public String updateProcess(@PathVariable("categoryName")String categoryName,
                                @PathVariable("id") Long id,
                                PostRequestDTO dto,

                                RedirectAttributes redirectAttributes){

        // 비회원 게시글인지 확인
        PostResponseDTO post = postService.readOnePost(id);
        boolean isGuestPost = post.getUserNickname() != null && post.getUserNickname().contains("(비회원)");

        if (!isGuestPost) {
            // 회원 게시글인 경우 기존 권한 확인
            if(!postService.isAccess(id)){
                redirectAttributes.addFlashAttribute("msg","글 수정 권한이 없습니다");
                return "redirect:/post/" + categoryName + "/" + id;
            }
        }

        try {
            postService.updateOnePost(id, dto);
            return "redirect:/post/" + categoryName + "/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msg", e.getMessage());
            return "redirect:/post/" + categoryName + "/" + id;
        }
    }




    //글 삭제 수행
    @PostMapping("/post/{categoryName}/deletePost/{id}")
    public String deleteProcess(@PathVariable("categoryName")String categoryName,@PathVariable("id") Long id,RedirectAttributes redirectAttributes){

        PostResponseDTO post = postService.readOnePost(id);
        boolean isGuestPost = post.getUserNickname() != null && post.getUserNickname().contains("(비회원)");

        if (isGuestPost) {
            // 비회원 게시글인 경우 비밀번호 확인 페이지로 리다이렉트
            redirectAttributes.addFlashAttribute("deletePostId", id);
            redirectAttributes.addFlashAttribute("categoryName", categoryName);
            return "redirect:/post/" + categoryName + "/" + id + "/delete-verify";
        } else {
            // 회원 게시글인 경우 기존 로직
            if(!postService.isAccess(id)){
                redirectAttributes.addFlashAttribute("msg","글 삭제 권한이 없습니다");
                return "redirect:/post/" + categoryName;
            }

            postService.deleteOnePost(id);
            return "redirect:/post/" + categoryName;
        }
    }

    // 비회원 게시글 삭제 비밀번호 확인 페이지
    @GetMapping("/post/{categoryName}/{id}/delete-verify")
    public String deleteVerifyPage(@PathVariable("categoryName") String categoryName,
                                   @PathVariable("id") Long id,
                                   Model model) {
        PostResponseDTO post = postService.readOnePost(id);
        model.addAttribute("POST", post);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("postId", id);
        return "guestpassworddelete";
    }

    // 비회원 게시글 삭제 수행
    @PostMapping("/post/{categoryName}/{id}/delete-verify")
    public String deleteGuestProcess(@PathVariable("categoryName") String categoryName,
                                     @PathVariable("id") Long id,
                                     @RequestParam("password") String password,
                                     RedirectAttributes redirectAttributes) {

            if(postService.isGuestAccess(id,password)){

                postService.deleteGuestPost(id, password);
                redirectAttributes.addFlashAttribute("msg", "게시글이 삭제되었습니다.");
                return "redirect:/post/" + categoryName;

            }else {

                redirectAttributes.addFlashAttribute("msg","비밀번호가 일치하지 않습니다.");
                return "redirect:/post/" + categoryName + "/" + id + "/delete-verify";
            }


        }



}


