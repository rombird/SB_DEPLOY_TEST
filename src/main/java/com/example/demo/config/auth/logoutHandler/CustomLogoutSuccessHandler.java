package com.example.demo.config.auth.logoutHandler;

import com.example.demo.config.auth.PrincipalDetails;
import com.example.demo.config.auth.jwt.JWTProperties;
import com.example.demo.domain.repository.JwtTokenRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component // Bean으로 만드는 작업을 해야 Client_id, redirect_uri를 들고 올 수 있다 -> CustomLogoutSuccessHandler을 securityConfig에 @Autowired  걸어주기
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}") // beans.factory.annotation으로 annotation지정, properties의 client-id 가져오기
    private String KAKAO_CLIENT_ID;

    @Value("${spring.security.oauth2.client.kakao.logout.redirect.uri}")
    private String KAKAO_REDIRECT_URI;

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    // 로컬서버 로그아웃 이후 추가처리(ex. 카카오인증서버 연결해제..)
    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager="jpaTransactionManager")
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("CustomLogoutSuccessHandler's onLogoutSuccess invoke...!!!!!!!");

        // AccessToken cookie 삭제  -> DB에 저장되어 있는정보 삭제
        String token=null; // access-token 쿠키 받아 token=null
        Cookie[] cookies = request.getCookies();
        if(cookies!=null){
            token = Arrays.stream(cookies)
                    .filter((cookie)->{return cookie.getName().equals(JWTProperties.ACCESS_TOKEN_COOKIE_NAME);})
                    .findFirst()
                    .map((cookie)->{return cookie.getValue();})
                    .orElse(null);
        }
        if(token!=null){
            // DB 제거
            jwtTokenRepository.deleteByAccessToken(token);
            // 쿠키 제거
            Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME, null);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }




        // OAUTH2 확인
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        String provider = principalDetails.getDto().getProvider();
        System.out.println("provider : " + provider); // 로그아웃 상태 확인: println(authentication)만 해도 확인 가능

        if(provider!=null && provider.startsWith("Kakao")){
            System.out.println("!!!" + KAKAO_CLIENT_ID + " " + KAKAO_REDIRECT_URI);
            // restful 에서 했던 logout3의 리다이렉트 경로
            response.sendRedirect("https://kauth.kakao.com/oauth/logout?client_id="+KAKAO_CLIENT_ID+"&logout_redirect_uri="+KAKAO_REDIRECT_URI);

            return;
        }else if(provider!=null && provider.startsWith("Naver")){
            response.sendRedirect("https://nid.naver.com/nidlogin.logout?returl=https://www.naver.com/");
            return; // -> naver.com 페이지로 이동

        }else if(provider!=null && provider.startsWith("Google")){
            response.sendRedirect("https://accounts.google.com/Logout");
            return;
        }

        // 기본경로로 이동하는 작업
        response.sendRedirect("/");
    }
}
