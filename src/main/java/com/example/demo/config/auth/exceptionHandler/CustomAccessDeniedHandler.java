package com.example.demo.config.auth.exceptionHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 로그인은 했지만 더 많은 권한이 필요할때 사용할 class
// 필요한 타입 -> AccessDeniedHandler 상속 -> 메서드 구현 -> handle 선택
@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler{
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error("CustomAccessDeniedHandler's handle invoke...!!!!!!!!!!!!" + accessDeniedException.getMessage());
    }

}
