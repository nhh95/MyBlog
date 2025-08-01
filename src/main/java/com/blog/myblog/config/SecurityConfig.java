package com.blog.myblog.config;

import com.blog.myblog.domain.user.entity.UserRoleType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    //비밀번호 암호화
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    //시큐리티 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        //csrf 해제
        http.csrf(csrf -> csrf.disable());

        //접근경로별 인가 설정

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/user/join").permitAll()
                .requestMatchers("/").permitAll()
                .requestMatchers("/user/update/**").hasRole("USER")
                .requestMatchers("/**").permitAll());


        //로그인 방식 설정
        http.formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .permitAll()
        );

        return http.build();

    }

    //ROLE 계층 추가
    @Bean
    public RoleHierarchy roleHierarchy(){

        return RoleHierarchyImpl.withRolePrefix("ROLE_")
                .role(UserRoleType.ADMIN.toString())
                .implies(UserRoleType.USER.toString())
                .build();
    }
}
