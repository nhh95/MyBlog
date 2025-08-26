package com.blog.myblog.controller;

import com.blog.myblog.domain.user.dto.UserRequestDTO;
import com.blog.myblog.domain.user.dto.UserResponseDTO;
import com.blog.myblog.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;


    //회원가입 페이지
    @GetMapping("/join")
    public String joinPage(Model model) {

        model.addAttribute("userRequestDTO",new UserRequestDTO());

        return "userpage/join";
    }
    //회원가입 수행
    @PostMapping("/user/join")
    public String joinProcess(@Valid @ModelAttribute("userRequestDTO") UserRequestDTO userRequestDTO,
                              BindingResult bindingResult,
                              RedirectAttributes ra) {

        if (bindingResult.hasErrors()) {
            return "userpage/join"; // 검증 실패 시 폼 재표시
        }

        try {

            userService.createOneUser(userRequestDTO);

        } catch (IllegalStateException e) { // 중복 이메일 등
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user/join";
        }

        return "redirect:/login";
    }

    //로그인 페이지
    @GetMapping("/login")
    public String loginPage() {
        return "userpage/login";
    }


    //회원수정 페이지
    @GetMapping("/user/update/{email}")
    public String updatePage(@PathVariable("email") String email, Model model) {
        if (!userService.isAccess(email)) {

            return "redirect:/login";
        }

        UserResponseDTO responseDTO = userService.readOneUser(email);

        // 수정 폼 바인딩용으로 RequestDTO로 옮김 (비밀번호는 빈으로)
        UserRequestDTO requestDTO = new UserRequestDTO();

        requestDTO.setNickname(responseDTO.getNickname());

        model.addAttribute("userRequestDTO", requestDTO);
        return "userpage/update";
    }


    @PostMapping("/user/update/{email}")
    public String updateProcess(@PathVariable String email,
                                @Valid @ModelAttribute("userRequestDTO") UserRequestDTO dto,
                                BindingResult br,
                                RedirectAttributes ra) {

        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.userRequestDTO", br);
            ra.addFlashAttribute("userRequestDTO", dto);
            return "redirect:/user/update/" + email;
        }

        if (userService.isAccess(email)) {
            userService.updateOneUser(dto, email);
        }

        return "redirect:/user/update/" + email;
    }
}
