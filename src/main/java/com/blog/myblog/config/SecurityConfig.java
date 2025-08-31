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
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

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

/*        http.csrf(csrf -> csrf.disable());*/



        http.csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        );


        //접근경로별 인가 설정
        http.authorizeHttpRequests(auth -> auth

                .requestMatchers("/post/userfreeboard/**", "/user/update/**").hasAnyRole("USER", "ADMIN")
/*
                .requestMatchers("/error").permitAll()




                .requestMatchers("/post/projectboard/**", "/post/guestfreeboard/**","/post/portfolioboard/**").permitAll()
                .requestMatchers("/post/projectpost/**", "/post/guestfreepost/**", "/post/portfoliopost/**").permitAll()

                .requestMatchers("/uploadSummernoteImageFile/**", "/deleteSummernoteImageFile/**","/summernoteimage/**").permitAll()


                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**","${file.temp.path}").permitAll()

*/

                .anyRequest().permitAll());


        //로그인 방식 설정
        http.formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)

                .usernameParameter("email")
                .passwordParameter("password")
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                .clearAuthentication(true)
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
