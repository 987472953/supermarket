package com.pzhu.youzaiyouzai.supermarket.admin.config;



import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author QYstart
 * @date 2021/6/22
 */
@Configuration
public class MyWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        AuthInterceptor first = new AuthInterceptor();
        registry.addInterceptor(first).addPathPatterns("/**").excludePathPatterns("/*/login");
    }
}
