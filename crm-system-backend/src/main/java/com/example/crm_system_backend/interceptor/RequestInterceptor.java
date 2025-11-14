package com.example.crm_system_backend.interceptor;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.service.serviceImpl.UserSessionService;
import com.example.crm_system_backend.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RequestInterceptor implements HandlerInterceptor {
    @Autowired
    private UserSessionService userSessionService;
    @Autowired
    private JwtUtil jwtUtil;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (uri.startsWith("/crm/")) {
            log.info("Request URI: {}", uri);

            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                return true; // allow CORS preflight to pass
            }

            String token = request.getHeader("Authorization");
            log.info("Token: {}", token);
            if (token == null || !token.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            token = token.substring(7).trim();

            //check expiry of token
            if(jwtUtil.isTokenExpired(token)){
                userSessionService.deleteSessionByEmail(jwtUtil.getEmail(token));
                response.setStatus(ErrorCode.SESSION_EXPIRED.getStatus().value());
                return false;
            }
            //check  if session is already present
            String email = jwtUtil.getEmail(token);
            if (email == null || !userSessionService.findSessionByToken(token,email)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            String role = jwtUtil.getRole(token);
            if(role == null){
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            Long id = jwtUtil.getId(token);
            request.setAttribute("role", role);
            request.setAttribute("userId", id);
            request.setAttribute("email", jwtUtil.getEmail(token));
        }

        return true;
    }


}
