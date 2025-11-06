package com.example.crm_system_backend.interceptor;

import com.example.crm_system_backend.annotations.RoleRequired;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RoleRequired roleRequired = handlerMethod.getMethodAnnotation(RoleRequired.class);
        if (roleRequired == null) {
            return true;
        }
        String role = request.getAttribute("role").toString();
        if(role == null){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        for (String requiredRole : roleRequired.value()) {
            if (requiredRole.equalsIgnoreCase(role)) {
                return true;
            }
        }

        log.error("User role {} is not authorized to access this resource", role);
        return false;

    }

}
