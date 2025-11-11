package com.example.demo.controller;

import com.example.demo.config.auth.PrincipalDetails;
import com.example.demo.domain.dtos.UserDto;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.security.Principal;

@Controller
@Slf4j
public class UserController {

    @Autowired
    private HttpServletResponse response;

    @Autowired
    private HttpServletRequest request;


    @GetMapping("/login")
    public void login(@AuthenticationPrincipal PrincipalDetails principalDetails) throws IOException {

        log.info("GET /login..." + principalDetails);
        // 페이지 유지하도록
        if(principalDetails!=null)
            response.sendRedirect("/user");
    }
    // DB연결 전 (SecurityConfig 권한처리 작업 후 -> 각각 전용페이지를 만들어 놓기)

//    // user정보를 확인하는 작업 - 1) Authentication Bean 주입
//    @GetMapping("/user")
//    public void user(Authentication authentication, Model model){
//        log.info("GET /user..." + authentication);
//        log.info("name..."+authentication.getName());
//        log.info("principal..."+authentication.getPrincipal());
//        log.info("authorities..."+authentication.getAuthorities()); // role 확인
//        log.info("details..."+authentication.getDetails()); // 상세정보(원격주소, 세션방식
//        log.info("credential..."+authentication.getCredentials()); // password (authentication수준에서 password는 가려짐)
//
//        // model을 쓰는 경우는 thymeleaf를 사용하는 경우에만!!!(확인용)
//        model.addAttribute("auth_1", authentication);
//    }

    // user정보를 확인하는 작업 - 2) 직접 ContextHolder에 접근해 꺼내서 사용
    @GetMapping("/user")
    public void user(Model model){
        // 주입하지않고 바로 꺼내올 수 있다는 의미 -> 활용빈도가 높다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("GET /user..." + authentication);
        log.info("name..."+authentication.getName());
        log.info("principal..."+authentication.getPrincipal());
        log.info("authorities..."+authentication.getAuthorities()); // role 확인
        log.info("details..."+authentication.getDetails()); // 상세정보(원격주소, 세션방식)
        log.info("credential..."+authentication.getCredentials()); // password (authentication수준에서 password는 가려짐)

        // model을 쓰는 경우는 thymeleaf를 사용하는 경우에만!!!(확인용)
        model.addAttribute("auth_1", authentication);
    }

    // user정보를 확인하는 작업 - 3) Authentication's Principal 만 꺼내와 연결
    @GetMapping("/manager")
    public void manager(@AuthenticationPrincipal PrincipalDetails principalDetails){
        log.info("GET /manager..." + principalDetails);
    }

    @GetMapping("/admin")
    public void admin(){
        log.info("GET /admin...");
    }

    @GetMapping("/join")
    public void join(){
        log.info("GET /join...");
    }

    // SecurityConfig에서 생성한 Bean PasswordEncoder에 대한 의존성 주입
    // Autowired는 제일 위에 처리할 것!
    @Autowired
    private PasswordEncoder passwordEncoder;

    // repository 연결
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/join")
    public String join_post(UserDto dto){ // join -> UserDto 만든 후 dto 받아오는지 확인하는 작업
        log.info("POST /join..." + dto);
        String pwd = passwordEncoder.encode(dto.getPassword()); // 암호화시키는 작업 <- 선 작업:Autowired PasswordEncoder
        // Dto -> Entity
        User user = new User(); // 클래스 생성 잘 해주기 domain에 있는 user
        user.setUsername(dto.getUsername());
        user.setPassword(pwd); // 암호화시킨 패스워드
        user.setRole("ROLE_USER"); // DB에 넣을 때는 ROLE_ 언더바까지 넣어줘야 판단 가능!!!!
        userRepository.save(user);
        boolean isJoin = true;
        if(isJoin){
            return "redirect:/login"; // join이 되었다면 login page 로
        }
        return "join"; // 문제가 있다면 join로 가도록
    }
    // -> UserEntity를 만들어서 저장하는 entity 필요!
}
