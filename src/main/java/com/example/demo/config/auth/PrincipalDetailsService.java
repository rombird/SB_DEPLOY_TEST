package com.example.demo.config.auth;

import com.example.demo.domain.dtos.UserDto;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

// 인증작업을 하기위해서 계정정보를 작업해야되는데 DB에 있는 정보를 전달해주는 역할
// 중요한 작업!!!!!!!!!!!!!!!!!!!
@Service
@Slf4j
public class PrincipalDetailsService implements UserDetailsService {
    // 인증작업은 일치하는지 안하는지
    // LOGIN FORM               -> SECURITY POST/login  -> PrincipalDetailsService
    // username : user1, 1234   -> user1, 1234          -> DB의 user1/암호화된 password를 꺼내서 전달

    @Autowired
    private UserRepository userRepository;

    // loginform에서 입력하는 username
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { // 계정이 있다면 UserDetails로 전달하는 구조
        System.out.println("PrincipalDetailsService's loadUserByUsername : " + username);

        Optional<User> userOptional = userRepository.findById(username); // 해당작업이 DB에 있다면

        // 계정이 없으면
        if(userOptional.isEmpty()) // userRepository에서 찾은 username이 없으면
            throw new UsernameNotFoundException(username + " 계정이 존재하지 않습니다."); // 없으면 예외로 던지고

        // 계정이 있으면 entity를 dto로 던지고 -> PrincipalDetails로 생성
        // ENTITY -> DTO
        User user = userOptional.get();
        UserDto dto = new UserDto();
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setRole(user.getRole());

        return new PrincipalDetails(dto);
    }
}
