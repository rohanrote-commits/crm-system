package com.example.crm_system_backend.interceptor;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.exception.UserException;
import com.example.crm_system_backend.service.serviceImpl.UserSessionService;
import com.example.crm_system_backend.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
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

    /**
     * Intercepts HTTP requests to perform pre-processing and authorization checks for requests
     * targeting resources with a URI that starts with "/crm/".
     * <ul>
     * - Validates the presence of a bearer token in the "Authorization" header.
     * - Checks if the token has expired and deletes the session in case of expiration.
     * - Verifies the presence of an active session associated with the token and user email.
     * - Extracts user role, ID, and email from the token and sets them as attributes in the request.
     * </ul>
     *
     * Failure to meet these conditions will result in an HTTP status code of 401 Unauthorized or other
     * relevant error codes.
     *
     * @param request  the HttpServletRequest object of the ongoing request
     * @param response the HttpServletResponse object for modifying the response
     * @param handler  the handler object to execute (can be used to retrieve additional metadata if needed)
     * @return true if the request passes all validations and the handler can proceed, otherwise false
     * @throws Exception if an underlying error or issue occurs during processing
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("Enter: RequestInterceptor.preHandle");
        String uri = request.getRequestURI();

        if (uri.startsWith("/crm/")) {
            log.info("Request URI: {}", uri);
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                return true;
            }

            String token = request.getHeader("Authorization");
            log.info("Token: {}", token);

            if (token == null || !token.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            token = token.substring(7).trim();

            String email;

            // ---- STEP 1: Extract email safely ----
            try {
                email = jwtUtil.getEmail(token);
            } catch (ExpiredJwtException ex) {
                email = ex.getClaims().getSubject();   // email from expired token

                // token expired → delete session → return error
                userSessionService.deleteSessionByEmail(email);

                response.setStatus(ErrorCode.SESSION_EXPIRED.getStatus().value());
                log.error("Exit: RequestInterceptor.preHandle with error: Session expired for user: {}", email);
                throw new UserException(ErrorCode.SESSION_EXPIRED);
            }

            // ---- STEP 2: Check expiry normally ----
            if (jwtUtil.isTokenExpired(token)) {
                userSessionService.deleteSessionByEmail(email);

                response.setStatus(ErrorCode.SESSION_EXPIRED.getStatus().value());
                log.error("Exit: RequestInterceptor.preHandle with error: Session expired for user: {}", email);
                throw new UserException(ErrorCode.SESSION_EXPIRED);
            }

            // ---- STEP 3: Validate active session ----
            if (!userSessionService.findSessionByToken(token, email)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                log.error("Exit: RequestInterceptor.preHandle with error: Another session active for user: {}", email);
                throw new UserException(ErrorCode.ANOTHER_SESSION_ACTIVE_FOR_USER);
            }

            // ---- STEP 4: Extract and set user details ----
            String role = jwtUtil.getRole(token);
            if (role == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                log.error("Exit: RequestInterceptor.preHandle with error: User not found for token: {}", token);
                throw new UserException(ErrorCode.USER_NOT_FOUND);
            }

            Long id = jwtUtil.getId(token);
            request.setAttribute("role", role);
            request.setAttribute("userId", id);
            request.setAttribute("email", email);
        }

        return true;
    }



}
