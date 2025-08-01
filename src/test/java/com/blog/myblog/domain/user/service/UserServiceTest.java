package com.blog.myblog.domain.user.service;

import com.blog.myblog.domain.user.dto.UserResponseDTO;
import com.blog.myblog.domain.user.entity.UserEntity;
import com.blog.myblog.domain.user.entity.UserRoleType;
import com.blog.myblog.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static com.blog.myblog.domain.user.entity.UserRoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;   // 가짜 Repository

    @InjectMocks
    private UserService userService;         // 테스트 대상(Service)

    @Test
    @DisplayName("readOneUser: 정상 조회 → DTO 매핑 검증")
    void readOneUser_success() {
        // given
        String email = "test@example.com";

        UserEntity entity = new UserEntity();
        entity.setEmail(email);
        entity.setNickname("Nam");
        entity.setRole(USER);


        when(userRepository.findByEmail(email)).thenReturn(Optional.of(entity));

        // when
        UserResponseDTO dto = userService.readOneUser(email);

        // then
        assertThat(dto.getEmail()).isEqualTo(email);
        assertThat(dto.getNickname()).isEqualTo("Nam");
        assertThat(dto.getRole()).isEqualTo(USER.toString());
    }

    @Test
    @DisplayName("readOneUser: 이메일이 없으면 NoSuchElementException")
    void readOneUser_notFound() {
        // given
        String email = "nobody@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NoSuchElementException.class,
                () -> userService.readOneUser(email));
    }
}