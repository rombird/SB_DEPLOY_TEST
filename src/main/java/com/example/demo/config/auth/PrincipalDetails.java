package com.example.demo.config.auth;

import com.example.demo.domain.dtos.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalDetails implements UserDetails, OAuth2User {
    // 데이터를 꺼내와서 최종적으로 Authentication에 저장됨

    private UserDto dto;
    Map<String, Object> attributes;

    public PrincipalDetails(UserDto dto){
        this.dto = dto;
    }

    // implements UserDetails 추가 -> 마우스 우클릭 -> 생성 -> 메서드 구현
    // 중요!!!!!!!!!!!
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { // GrantedAuthority클래스형으로 상속관계에 있는 하위 클래스로 처리를 해줘야함
        // getAuthorities : role 꺼낼 때 쓰는 작업
        Collection<GrantedAuthority> authorities = new ArrayList<>(); //컬렉션 하위리스트이 arraylist

        // 계정이 단일 ROLE을 가질 때("ROLE_USER")
        // authorities.add(new SimpleGrantedAuthority(dto.getRole()));

        // 계정이 여러 ROLE을 가질 때("ROLE_ADMIN, ROLE_USER")
        String roles [] = dto.getRole().split(","); // ,로 구분했으니 DB에 저장할 때 ,로만 구분해서 지정(띄어쓰기 X)
        for(String role : roles){
            authorities.add(new SimpleGrantedAuthority(role));
        }

        return authorities;
    } // 이 설정을 안한다면 로그인은 했지만 접근 권한 없어짐
    // security가 꺼내서 사용

    // ----------------------------------------------------
    // OAUTH2에 사용되는 속성/메서드 - OAuth2User implements에 추가 -> 메서드 구현
    // ----------------------------------------------------
    // attribute를 적절하게 이용하기 때문에 거기에 맞게 속성도 추가

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }


    // ----------------------------------------------------
    // 로컬인증에 사용되는 메서드
    // ----------------------------------------------------
    @Override
    public String getPassword() {
        return dto.getPassword(); // 전달받은 패스워드 꺼내주는 작업
    }

    @Override
    public String getUsername() {
        return dto.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() { // 만료 안되었는지 확인(ex. 회원가입후 90일 이후 만료됨)
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "";
    }
}
