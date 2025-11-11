package com.example.demo.config;

import com.example.demo.config.auth.exceptionHandler.CustomAccessDeniedHandler;
import com.example.demo.config.auth.exceptionHandler.CustomAuthenticationEntryPoint;
import com.example.demo.config.auth.jwt.JWTAuthorizationFilter;
import com.example.demo.config.auth.loginHandler.CustomFailureHandler;
import com.example.demo.config.auth.loginHandler.CustomSuccessHandler;
import com.example.demo.config.auth.logoutHandler.CustomLogoutHandler;
import com.example.demo.config.auth.logoutHandler.CustomLogoutSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity // 기존 Security 설정이 아니라 여기서 직접 Security를 설정하겠다는 의미
public class SecurityConfig {
    // Bean 주입 (각각의 파일에서 component로 만든 다음)
    @Autowired
    CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Autowired
    CustomFailureHandler customFailureHandler;

    @Autowired
    CustomSuccessHandler customSuccessHandler;

    @Autowired
    CustomLogoutHandler customLogoutHandler;

    @Autowired
    CustomLogoutSuccessHandler customLogoutSuccessHandler;

    @Autowired
    JWTAuthorizationFilter jwtAuthorizationFilter;


    // 기본 설정 코드
    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        // 외부로부터 security 객체 주소가 전달되면 build 작업을 통해 리턴

        // csrf 비활성화(비활성화하지 않으면 logout 요청은 기본적으로 POST 방식을 따른다) -> login.html에서
        http.csrf((config)->{config.disable();});

        // 권한처리
        http.authorizeHttpRequests((auth)->{
            auth.requestMatchers("/", "/join", "/login").permitAll(); // 해당 페이지(기본, 회원가입, 로그인)는 권한 허용할 수 있도록
            auth.requestMatchers("/user").hasAnyRole("USER"); // ROLE_USER 권한을 가진 사용자만 접근 가능
            auth.requestMatchers("/manager").hasAnyRole("MANAGER"); // ROLE_MANAGER 권한을 가진 사용자만 접근 가능
            auth.requestMatchers("/admin").hasAnyRole("ADMIN"); // ROLE_ADMIN 권한을 가진 사용자만 접근 가능
            auth.anyRequest().authenticated(); // 그 외 나머지처리는 인증이 되도록 - 위에서 지정되지 않은 나머지 모든 요청은 '인증'된 사용자만 접근 가능
        });

        // 로그인 // 람다식으로 처리해줘야함
        http.formLogin((login)->{
            login.permitAll(); // 로그인페이지 누구나 접근 가능하도록
            login.loginPage("/login"); // 엔드포인트
            login.successHandler(customSuccessHandler); // 로그인 성공시 동작하는 핸들러
            login.failureHandler(customFailureHandler); // 로그인 실패시 동작하는 핸들러(ID 미존재, PW 불일치)
            // new CustomSuccessHandler/new CustomFailureHandler -> 추가액션으로 implement 자동생성
        });

        // 로그아웃(설정시 POST 처리) // 람다식으로 처리해줘야함
        http.logout((logout)->{
            logout.permitAll();
            // new CustomLogoutHandler/new CustomLogoutSuccessHandler  -> 추가액션
            logout.addLogoutHandler(customLogoutHandler); // 로그아웃 처리 핸들러
            logout.logoutSuccessHandler(customLogoutSuccessHandler); // new CustomLogoutSuccessHandler()를 사용X -> bean으로 만들어주는 작업을 했기 때문에 사용 가능
        });

        // 예외처리
        http.exceptionHandling((ex)->{
            ex.authenticationEntryPoint(customAuthenticationEntryPoint); // 미인증된 상태(로그인하지 않은)에서 권한이 필요한 endpoint로 접근시 예외처리
            ex.accessDeniedHandler(customAccessDeniedHandler); // 인증은 되었으나 접근 권한(Role)이 부족할 때
        });

        // Oauth2-Client 활성화
        http.oauth2Login((oauth2)->{
            oauth2.loginPage("/login");
        });

        // SESSION 비활성화
        http.sessionManagement((sessionConfig)->{
           sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS); // 세션을 사용할 때 사용할 적절한 상수값 넣어주기
        });

        // TokenFilter 추가
        http.addFilterBefore(jwtAuthorizationFilter, LogoutFilter.class); // logout 처리되기 이전에 필터 처리되도록

        // Etc...
        return http.build();
    }

    // 패스워드 암호화작업(해시값생성)에 사용되는 Bean
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
