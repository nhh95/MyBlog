package com.blog.myblog.domain.user.service;

import com.blog.myblog.domain.user.dto.UserRequestDTO;
import com.blog.myblog.domain.user.dto.UserResponseDTO;
import com.blog.myblog.domain.user.entity.UserEntity;
import com.blog.myblog.domain.user.entity.UserRoleType;
import com.blog.myblog.domain.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder){

        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }


    //유저 한명 만들기
    @Transactional
    public void createOneUser(UserRequestDTO dto) {

        String email = dto.getEmail();
        String password = dto.getPassword();
        String nickname = dto.getNickname();

        log.info("회원가입 시도: email={}, nickname={}", email, nickname);

        //동일한 email이 있는지 확인
        if(userRepository.existsByEmail(email)) {

            log.warn("이미 존재하는 이메일로 가입 시도: {}", email);

            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }

        //UserEntity 생성
        UserEntity entity = new UserEntity();

        entity.setEmail(email);
        entity.setPassword(bCryptPasswordEncoder.encode(password));
        entity.setNickname(nickname);
        entity.setRole(UserRoleType.USER);

        userRepository.save(entity);

        log.info("회원가입 성공: email={}", email);
    }

    //유저 한명 읽기
    @Transactional
    public UserResponseDTO readOneUser(String email) {

        UserEntity entity = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일을 가진 유저를 찾을 수 없습니다: " + email));

        UserResponseDTO dto = new UserResponseDTO();

        dto.setEmail(entity.getEmail());
        dto.setNickname(entity.getNickname());
        dto.setRole(entity.getRole().toString());

        return dto;
    }

    //모든 유저 읽기
    @Transactional(readOnly = true)
    public List<UserResponseDTO> readAllUsers() {

        List<UserEntity> list = userRepository.findAll();

        List<UserResponseDTO> dtos = new ArrayList<>();
        for(UserEntity user : list) {

            UserResponseDTO dto = new UserResponseDTO();

            dto.setEmail(user.getEmail());
            dto.setNickname(user.getNickname());
            dto.setRole(user.getRole().toString());
            dto.setCreatedAt(user.getCreatedAt().toString());

            dtos.add(dto);

        }

        return dtos;

    }

    // 시큐리티 전용 로그인 메서드
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{

        log.debug("loadUserByUsername 호출: email={}", email);

        UserEntity entity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 이메일을 찾을 수 없습니다: " + email));


        return User.builder()
                .username(entity.getEmail())
                .password(entity.getPassword())
                .roles(entity.getRole().toString())
                .build();


    }


    //유저 한명 수정
    @Transactional
    public void updateOneUser(UserRequestDTO dto,String email) {

        UserEntity entity = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("수정 대상 유저를 찾을 수 없습니다: " + email));

        if(dto.getPassword() != null && !dto.getPassword().isEmpty()){
            entity.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
        }

        if(dto.getNickname() != null && !dto.getNickname().isEmpty()) {
            entity.setNickname(dto.getNickname());
        }

        userRepository.save(entity);

    }

    //유저 한명 삭제
    @Transactional
    public void deleteOneUser(String email){

        userRepository.deleteByEmail(email);

    }

    //유저 접근 권한 확인
    public boolean isAccess(String email){

        //현재 로그인 되어있는 유저의 이메일
        String sessionEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        //현재 로그인 되어있는 유저의 role
        String sessionRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority();

        //ADMIN 전부 접근 가능
        if("ROLE_ADMIN".equals(sessionRole)) {
            return true;
        }

        //작성자 email 이 로그인한 email 과 같은지 확인

        if(email.equals(sessionEmail)){
            return true;
        }

        return false;
    }


}
