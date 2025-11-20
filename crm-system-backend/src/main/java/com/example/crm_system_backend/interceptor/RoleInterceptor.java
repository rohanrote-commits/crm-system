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
    /**
     * Intercepts and handles HTTP requests before they reach the controller, performing
     * role-based authorization for methods annotated with {@link RoleRequired}.
     *
     * If the handler is not of type {@link HandlerMethod}, the method allows the request
     * to proceed without additional checks. If a method is annotated with {@link RoleRequired},
     * the user's role is validated against the required roles specified in the annotation.
     * If no valid role is found or the role is null, the method responds with an HTTP 401 Unauthorized
     * status code and blocks the request.
     *
     * @param request  the {@link HttpServletRequest} object of the ongoing request
     * @param response the {@link HttpServletResponse} object for modifying the response
     * @param handler  the handler object for retrieving method metadata, typically of type {@link HandlerMethod}
     * @return true if the user's role is authorized or no role restrictions are specified,
     *         false if the role is not authorized or is null
     * @throws Exception if an underlying error occurs during the request handling process
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("Enter: RoleInterceptor.preHandle");

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RoleRequired roleRequired = handlerMethod.getMethodAnnotation(RoleRequired.class);
        if (roleRequired == null) {
            return true;
        }
        String role = request.getAttribute("role").toString();
        if (role == null) {
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
