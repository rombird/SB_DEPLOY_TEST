package com.example.demo.config.auth.loginHandler;

import com.example.demo.config.auth.PrincipalDetails;
import com.example.demo.config.auth.jwt.JWTProperties;
import com.example.demo.config.auth.jwt.JWTTokenProvider;
import com.example.demo.config.auth.jwt.TokenInfo;
import com.example.demo.domain.entity.JwtToken;
import com.example.demo.domain.repository.JwtTokenRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    JWTTokenProvider jwtTokenProvider; // 토큰 생성기

    @Autowired
    JwtTokenRepository jwtTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 로그인했을 때 accesstoken이 cookie로 전달되는 과정
        // TOKEN 을 COOKIE로 전달
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);
        // cookie로 던져주는 작업 cookie import -> jakarta
        Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME, tokenInfo.getAccessToken());
        cookie.setMaxAge(JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME); // accesstoken 유지시간
        cookie.setPath("/"); // 쿠키 적용경로(/ : 모든 경로)
        response.addCookie(cookie); // 응답정보에 쿠키 포함

        PrincipalDetails principalDetails = (PrincipalDetails)authentication.getPrincipal();
        String auth = principalDetails.getDto().getRole();
        // TOKEN 을 DB에 저장
        JwtToken tokenEntity = JwtToken.builder()
                                    .accessToken(tokenInfo.getAccessToken())
                                    .refreshToken(tokenInfo.getRefreshToken())
                                    .username(authentication.getName())
                                    .auth(auth)
                                    .createAt(LocalDateTime.now())
                                    .build();
        jwtTokenRepository.save(tokenEntity);


        log.info("CustomSuccessHandler's onAuthenticationSuccess invoke...genToken....." + tokenInfo);


        // ROLE 별로 redirect 경로 수정
        String redirectUrl="/";
//        for(GrantedAuthority authority : authentication.getAuthorities()){
//            log.info("authority : " + authority);
//            String role = authority.getAuthority(); // String
//            // 권한 높은 순서대로
//            if(role.contains("ROLE_ADMIN")){
//                // admin으로 리다이렉트
//                redirectUrl="/admin";
//                break;
//            }else if(role.contains("ROLE_MANAGER")){
//                // manager 리다이렉트
//                redirectUrl="/manager";
//                break;
//            }else{
//                // user 리다이렉트
//                redirectUrl="/user";
//                break;
//            }
//        }
        response.sendRedirect(redirectUrl);

    }
}
