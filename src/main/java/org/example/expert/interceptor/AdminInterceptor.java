package org.example.expert.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Slf4j
public class AdminInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AdminInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        AuthUser authUser = (AuthUser) request.getAttribute("authUser");

        if (authUser == null || authUser.getUserRole() != UserRole.ADMIN) {
            throw new AuthException("어드민만 접근할 수 있습니다.");
        }

        logger.info("[ADMIN INTERCEPT] URL: {}, User ID: {}, Time: {}",
                request.getRequestURI(),
                authUser.getId(),
                LocalDateTime.now());

        return true;
    }
}

