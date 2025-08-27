package com.blog.myblog.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.temp.path}")
    private String tempFilePath;

    @Value("${file.final.path}")
    private String finalFilePath;



    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // file: 뒤에 tempFilePath 변수를 사용합니다.
        // C:/summernote_image/temp/ 경로를 URL의 /summernoteImage/ 로 매핑합니다.
        registry.addResourceHandler("/summernoteImage/**")
                .addResourceLocations("file:///" + tempFilePath + "/");

    }

/*    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public void postHandle(HttpServletRequest request, HttpServletResponse response,
                                   Object handler, ModelAndView modelAndView) {
                // 응답이 이미 커밋되지 않은 경우에만 헤더 설정
                if (!response.isCommitted()) {
                    response.setHeader("X-Robots-Tag", "noindex, nofollow, noarchive");
                }
            }
        });

    }*/


}