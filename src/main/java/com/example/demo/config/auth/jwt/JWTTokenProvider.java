package com.example.demo.config.auth.jwt;

import com.example.demo.config.auth.PrincipalDetails;
import com.example.demo.domain.dtos.UserDto;
import com.example.demo.domain.entity.Signature;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.SignatureRepository;
import com.example.demo.domain.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JWTTokenProvider { // 토큰 생성기

    @Autowired
    private UserRepository userRepository; // accesstoken과 거기에 맞는 계정이 실제로 존재한느지부터 체크

    @Autowired
    private SignatureRepository signatureRepository;

    // key
    private Key key; // Security import로 선택

    // 임의로 JWTTokenProviderTest에서 사용하기 위해
    public Key getKey(){
        return key;
    }

    // 기본 함수
    @PostConstruct // 생성자가 생성된 이후에 동작하는 기본 함수를 지정할 때 사용
    public void init(){
        // 서명을 저장해서 DB에 저장
        List<Signature> list = signatureRepository.findAll();
        if(list.isEmpty()){
            byte[] keyBytes = KeyGenerator.keyGen();
            this.key = Keys.hmacShaKeyFor(keyBytes);
            Signature signature = new Signature();
            signature.setKeyBytes(keyBytes);
            signature.setCreateAt(LocalDate.now());
            signatureRepository.save(signature);
        }else{
            Signature signature = list.get(0);
            this.key = Keys.hmacShaKeyFor(signature.getKeyBytes());
        }


        byte[] keyBytes = KeyGenerator.keyGen();
        this.key = Keys.hmacShaKeyFor(keyBytes); // 복호화나 여러가지 알고리즘 기법이 들어간 함수 사용가능
    }

    public TokenInfo generateToken(Authentication authentication){

        // 계정정보 - 계정명 / auth(role)
        // authentication정보를 전달받았다고 가정하고 작업
        String authorities = authentication.getAuthorities() // Collection<SimpleGrantedAuthority> authorities 반환
                .stream() // Stream 함수 사용 예정
                .map((role)->{return role.getAuthority();}) // 각각 GrantedAuthority("ROLE_~")들을 문자열 값으로 반환해서 map처리
                .collect(Collectors.joining(",")); // 각각의 role(ROLE_ADMIN, ROLE_USER) 를 ',' 를 기준으로 묶음("ROLE_USER, ROLE_ADMIN")

        // AccessToken(서버의 서비스를 이용제한)
        long now = (new Date()).getTime(); // 현재 시간
        String accessToken = Jwts.builder()
                            .setSubject(authentication.getName()) // 본문 TITLE
                            .setExpiration(new Date(now+JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME)) // 만료날짜(밀리초단위) : 현재시간+access token 만료시간
                            .signWith(key, SignatureAlgorithm.HS256) // 서명값
                            .claim("username",authentication.getName()) // 본문 내용
                            .claim("auth", authorities)
                            .compact();

        // RefreshToken(AccessToken 만료시 갱신처리) - Access Token을 짧게 잡고 액세스 토큰을 만료되었을 때 갱신을 위한 토큰
        String refreshToken = Jwts.builder()
                            .setSubject("Refresh_Token_Title") // 본문 TITLE
                            .setExpiration(new Date(now+JWTProperties.REFRESH_TOKEN_EXPIRATION_TIME)) // 만료날짜(밀리초단위) : 현재시간+refresh token 만료시간
                            .signWith(key, SignatureAlgorithm.HS256) // 서명값
                            .compact();

        // TokenInfo(토큰을 생성하는 token을 bean으로 생성)
        return TokenInfo.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // getAuthentication을 AuthorizationFilter에
//    토큰을 받아서 authentication을 반환하는 getautentication

    public Authentication getAuthentication(String accessToken)throws ExpiredJwtException{
        // 시간차이에서 만료가 발생하면 throws로 던져주고
        // accesstoken을 받아서 username, role을 빼와서 dto
        Claims claims = Jwts.parser().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();

        String username = claims.getSubject(); // username
        username = (String)claims.get("username"); // username
        String auth = (String)claims.get("auth"); // ROLE_USER, ROLE_ADMIN

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        String roles [] = auth.split(","); // ["ROLE_USER", "ROLE_ADMIN"]
        for(String role : roles){
            authorities.add(new SimpleGrantedAuthority(role));
        }


        PrincipalDetails principalDetails = new PrincipalDetails(); // Authentication에 들어갈 항목
        UserDto dto = null;

        if (userRepository.existsById(username)){ // 계정이 있는지 여부만 체크

            dto = new UserDto();
            dto.setUsername(username);
            dto.setRole(auth);
            dto.setPassword(null);

            principalDetails = new PrincipalDetails(dto);
        }
        if(principalDetails!=null){ // principalDetails에 문제 없다면 authenticationToken을 발행하고
            UsernamePasswordAuthenticationToken authenticationToken
                    = new UsernamePasswordAuthenticationToken(principalDetails,"", authorities);
            return authenticationToken; // authentication을 직접 설정
        }

        return null;
    }





    // validateToken을 AuthorizationFilter에 유효성 여부 확인하기위해 사용
    public boolean validateToken(String token) throws Exception {
        // 토큰이 유효한지 아닌지 boolean으로 대답하도록
        boolean isValid = false;
        try{
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token); // 토큰이 만료가 되거나 유효하지 않은 서명키 등등 오류가 발생 -> try catch로 묶어서 사용
            isValid = true;
        }catch(ExpiredJwtException e){
            // 만료 예외
            log.info("[ExpiredJwtException].. " + e.getMessage());
            throw new ExpiredJwtException(null, null, null); // header, claims, message
        }
        return isValid;
    }

}
