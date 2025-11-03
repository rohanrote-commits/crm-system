package com.example.crm_system_backend.interceptor;

import com.example.crm_system_backend.handler.AuthHandler;
import com.example.crm_system_backend.service.serviceImpl.UserSessionService;
import com.example.crm_system_backend.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestInterceptor implements HandlerInterceptor {
    @Autowired
    private UserSessionService userSessionService;
    @Autowired
    private JwtUtil jwtUtil;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return true;
        }

        token = token.substring(7);
        Long id = jwtUtil.getId(token);

        request.setAttribute("registeredById", id);
        return true;
    }

}
