package org.example.expert.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
public class AdminLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(AdminLoggingAspect.class);
    private final ObjectMapper objectMapper;

    public AdminLoggingAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // 관리자가 접근하는 API만 타겟팅
    @Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..)) || " +
            "execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    public void adminOnlyEndpoints() {}

    @Around("adminOnlyEndpoints()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 요청 정보
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String url = request.getRequestURI(); // 요청 URL
        String method = request.getMethod(); // HTTP Method
        Long userId = (Long) request.getAttribute("userId");

        // 요청 바디 (RequestBody만 직렬화)
        String reqJson = "";
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < targetMethod.getParameters().length; i++) {
            if (targetMethod.getParameters()[i].isAnnotationPresent(RequestBody.class)) {
                try {
                    reqJson = objectMapper.writeValueAsString(args[i]);
                } catch (Exception e) {
                    reqJson = "[변환 실패]";
                }
            }
        }

        // 실제 메서드 실행 (응답 본문 직렬화)
        Object result = joinPoint.proceed();

        // 응답 바디 직렬화
        String resJson;
        try {
            resJson = objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            resJson = "[변환 실패]";
        }

        // 로그 출력
        log.info("[AOP] {} | {} | 사용자 ID: {} | URL: {}\nRequest: {}\nResponse: {}",
                LocalDateTime.now(), method, userId != null ? userId : "알 수 없음", url, reqJson, resJson);

        return result;
    }
}
