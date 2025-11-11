package com.example.demo.config.auth.jwt;

import com.example.demo.domain.entity.JwtToken;
import com.example.demo.domain.repository.JwtTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

// 사용자로부터 액세스 토큰을 받아서 ...하는 파일 토큰 방식의 인증처리를 위해
@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter { // Security에 사용하는 필터로 만들 것
    // 인증 이후에 권한 부여에 사용하는 필터(토큰을 사용하는)

    // JWTTokenProvider만든 ValidateToken 사용을 위해
    @Autowired
    JWTTokenProvider jwtTokenProvider;

    @Autowired
    JwtTokenRepository jwtTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 전

        System.out.println("[JWTAuthorizationFilter] doFilterInternal...");

        // access-token 쿠키 받기
        // 사용자가 요청을 하면 브라우저에 전달되는 모든 쿠키 확인 - "access-token" 만 (배열형태로)
        String token=null; // access-token 쿠키 받아 token=null
        Cookie[] cookies = request.getCookies(); // 애초에 cookies가 null이면 문제가 생길 수도 있음
        if(cookies!=null){
            token = Arrays.stream(cookies) // request로 받은 모든 (배열형태의) cookies를 cookie로 하나씩 꺼낼 것
                    .filter((cookie)->{return cookie.getName().equals(JWTProperties.ACCESS_TOKEN_COOKIE_NAME);}) // 쿠키의 이름이 access-token과 일치하는지 (properties에서 정해놓은 이름과)
                    .findFirst()        // 검색되는 첫번째 access-token을 가져옴 (access-token 의 value 값을 꺼내와야함)
                    .map((cookie)->{return cookie.getValue();})
                    .orElse(null); // 해당 작업의 내용이 없으면 null로 처리
        }

        System.out.println("TOKEN : " + token);
        if(token!=null){
            // access-token 쿠키 받아 -> Authentication 생성 이후 SecurityContextHolder에 저장

            // 1) access-token 만료되었는지 확인
            try{
                if(jwtTokenProvider.validateToken(token)){ // access token 유효성 체크 : validateToken 에서 true, false 값이 나올 텐데 ->
                    // 1-1) access-token==만료되지 x -> Authentication 생성하는 작업 필요
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    if(authentication!=null)
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                }
            }catch(ExpiredJwtException e){
                // 1-2) access-token==만료 o ? refresh-token 만료 여부 확인 (이후에 작업)
                System.out.println("ExpiredJwtException...AccessToken Expired.." + e.getMessage()); // accesstoken 만료된 이후에 들어온 것

                // 2) RefreshToken 의 만료 유무
                JwtToken entity = jwtTokenRepository.findByAccessToken(token);
                if(entity!=null){

                    try{
                        if(jwtTokenProvider.validateToken(entity.getRefreshToken())){
                            // 2-1) RefreshToken != 만료 -> AccessToken 재발급 -> 쿠키전달 + DB Token Info 갱신
                            // Access Token 재발급(갱신)
                            long now = (new Date()).getTime(); // 현재 시간
                            String accessToken = Jwts.builder()
                                    .setSubject(entity.getUsername()) // 본문 TITLE
                                    .setExpiration(new Date(now+JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME)) // 만료날짜(밀리초단위) : 현재시간+access token 만료시간
                                    .signWith(jwtTokenProvider.getKey(), SignatureAlgorithm.HS256) // 서명값
                                    .claim("username",entity.getUsername()) // 본문 내용
                                    .claim("auth", entity.getAuth()) // ROLE_USER, ROLE_ADMIN
                                    .compact();

                            // 쿠키로 전달
                            // cookie로 던져주는 작업 cookie import -> jakarta
                            Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME, accessToken);
                            cookie.setMaxAge(JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME); // accesstoken 유지시간
                            cookie.setPath("/"); // 쿠키 적용경로(/ : 모든 경로)
                            response.addCookie(cookie);
                            // DB's AccessToken 작업
                            entity.setAccessToken(accessToken);
                            jwtTokenRepository.save(entity);
                        }
                    }catch(ExpiredJwtException e2){
                        // 2-2) RefreshToken == 만료 ? -> DB's token Info 삭제
                        System.out.println("ExpiredJwtException ... RefreshToken Expired.. " + e2.getMessage());
                        // access-token 제거(자동제거는 됨) -> 로그인하고 30초 지나면 access-token 사라짐
                        Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME, null);
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);

                        jwtTokenRepository.deleteById(entity.getId()); // 만료시 DB에 해당 정보 삭제
                    }catch(Exception e2){

                    }



                }

            }catch(Exception e){

            }

        }else{
            // access-token == null
            // 1) 최초로그인(DB에도 X, 최초 발급)
            // 2) access-token 발급 받았으나 쿠키 만료(==token 만료)시간에 의해서 제거된 상태(DB에는 존재, 쿠키 만료)

        }

        filterChain.doFilter(request, response); // 다음 위치로 이동하도록

        // 후


    }


}
