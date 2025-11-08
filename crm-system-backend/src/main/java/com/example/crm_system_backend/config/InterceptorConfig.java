package com.example.crm_system_backend.config;

import com.example.crm_system_backend.interceptor.RequestInterceptor;
import com.example.crm_system_backend.interceptor.RoleInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private RequestInterceptor requestInterceptor;

    @Autowired
    private RoleInterceptor roleInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor)
                .addPathPatterns("/crm/user/**","/api/crm/lead")
                .excludePathPatterns("/crm/user/sign-in", "/crm/user/sign-up","/crm/user/forget");

        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/crm/user/**")
                .excludePathPatterns("/crm/user/sign-in", "/crm/user/sign-up","/crm/user/forget");

    }
}
