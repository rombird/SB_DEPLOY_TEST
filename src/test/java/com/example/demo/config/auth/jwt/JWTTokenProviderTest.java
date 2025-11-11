//package com.example.demo.config.auth.jwt;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.JwtParser;
//import io.jsonwebtoken.Jwts;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.security.Key;
//
//import static org.junit.jupiter.api.Assertions.*;
//@SpringBootTest
//class JWTTokenProviderTest {
//
//    @Autowired
//    private JWTTokenProvider tokenProvider;
//
//    @Test
//    public void t1() throws Exception{
//        TokenInfo tokenInfo = tokenProvider.generateToken(); // 토큰 생성
//        System.out.println(tokenInfo);
//        Key key = tokenProvider.getKey();
//
//        // 암호화, 복호화한 key 꺼내는 작업
//        JwtParser parser = Jwts.parser()
//                                .setSigningKey(key) // key 추가
//                                .build();
//
//        String accessToken = tokenInfo.getAccessToken();
//        Claims claims = parser.parseClaimsJws(accessToken).getBody();
//        String username = claims.get("username").toString();
//        String role = claims.get("role").toString();
//        System.out.println("username : "+username);
//        System.out.println("role : "+role);
//    }
//    // 토큰을 사용해서 쿠키를 생성 -> filter 사용
//
//    @Test
//    public void t2() throws Exception{
//
//    }
//
//}