package com.example.springbootboilerplate.global.config;

import com.example.springbootboilerplate.global.logging.RequestIdInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RequestIdInterceptor requestIdInterceptor;

    // 인터셉터 등록
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 모든 경로에 대해 RequestIdInterceptor를 등록하되, 특정 경로는 제외
        registry.addInterceptor(requestIdInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/**",
                        "/health"
                );
    }
}
