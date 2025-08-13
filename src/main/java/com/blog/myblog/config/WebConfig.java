package com.blog.myblog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
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
                .addResourceLocations("file:///" + tempFilePath + "/")
                .addResourceLocations("file:///" + finalFilePath + "/");
    }
}