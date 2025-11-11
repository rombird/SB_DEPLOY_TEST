package com.example.demo.config.auth;

import com.example.demo.config.auth.provider.GoogleUserInfo;
import com.example.demo.config.auth.provider.KakaoUserInfo;
import com.example.demo.config.auth.provider.NaverUserInfo;
import com.example.demo.config.auth.provider.OAuth2UserInfo;
import com.example.demo.domain.dtos.UserDto;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class PrincipalDetailsOAuth2Service extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        System.out.println("");
//        System.out.println("userRequest : " + userRequest);
//        System.out.println("userRequest.getClientRegistration() : " + userRequest.getClientRegistration());
//        System.out.println("userRequest.getAccessToken() : " + userRequest.getAccessToken());
//        System.out.println("userRequest.getAdditionalParameters() : " + userRequest.getAdditionalParameters());
//        System.out.println("userRequest.getAccessToken().getTokenValue() : " + userRequest.getAccessToken().getTokenValue());
//        System.out.println("userRequest.getAccessToken().getTokenType().getValue() : " + userRequest.getAccessToken().getTokenType().getValue());
//        System.out.println("userRequest.getAccessToken().getScopes() : " + userRequest.getAccessToken().getScopes());
        System.out.println("");

        OAuth2User oAuth2User = super.loadUser(userRequest); // 상위 클래스에 있는 loadUser 사용
        System.out.println("oAuth2User : " + oAuth2User);
        System.out.println("oAuth2User.getAttributes() : " + oAuth2User.getAttributes()); // 사용자의 정보를 담고 있는 attributes
        System.out.println("Provider Name : " + userRequest.getClientRegistration().getClientName()); // application.properties에 등록했던 Name : Kakao

        String provider = userRequest.getClientRegistration().getClientName();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2UserInfo oAuth2UserInfo = null;
        String username=null; // 밖에서도 사용할 수 있도록 전역화
        // 카카오라면
        if(provider.startsWith("Kakao")){
            // 카카오 객체를 만드는 코드(객체화 작업)
            Long id = (Long)attributes.get("id");
            LocalDateTime connected_at = OffsetDateTime.parse(attributes.get("connected_at").toString()).toLocalDateTime();
            Map<String, Object> properties = (Map<String, Object>)attributes.get("properties");
            Map<String, Object> kakao_account = (Map<String, Object>)attributes.get("kakao_account");
            System.out.println("id : " + id);
            System.out.println("connected_at : " + connected_at);
            System.out.println("properties : "+properties);
            System.out.println("kakao_account :"+ kakao_account);
            // upcasting 작업, provider 안에 oAuth2UserInfo : 공통속성을 담는 인터페이스 만듦
            oAuth2UserInfo = KakaoUserInfo.builder()
                    .id(id)
                    .connected_at(connected_at)
                    .properties(properties)
                    .kakao_account(kakao_account)
                    .build();

            // DB 등록예정 계정명 - 카카오 전용 username 지정
            username=oAuth2UserInfo.getProvider() + "_" + oAuth2UserInfo.getProviderId(); // 임시계정 username = Kakao_4454230216

        }
        else if(provider.startsWith("Naver")){
            Map<String, Object> response = (Map<String, Object>)attributes.get("response");
            System.out.println("response : " + response);
            oAuth2UserInfo = NaverUserInfo.builder()
                    .response(response)
                    .build();

            // DB 등록예정 계정명
            username = oAuth2UserInfo.getEmail();

        }
        else if(provider.startsWith("Google")){
            oAuth2UserInfo = GoogleUserInfo.builder()
                    .attributes(attributes)
                    .build();

            // DB 등록예정 계정명
            username = oAuth2UserInfo.getEmail();
        }
        System.out.println("oAuth2UserInfo : " + oAuth2UserInfo);
        // OAuth2 정보를 기반으로 -> 로컬계정 생성(계정 X : 생성, 계정 O : 기존계정 불러오기)

        String password=passwordEncoder.encode("1234");

        // 기존 계정 존재 여부에 따라 DB 저장
        Optional<User> userOptional = userRepository.findById(username);
        UserDto dto = null;
        if(userOptional.isEmpty()){
            // 기존계정이 없다면 entity로 바꿔서 넣어주기(user 만들고 dto로도 만들기)
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setRole("ROLE_USER");
            userRepository.save(user);

            dto = new UserDto(username, password, "ROLE_USER");

        }else{
            User user = userOptional.get();
            dto = new UserDto(username, user.getPassword(), user.getRole());
        }

        // PrincipalDetails 로 변환해서 반환 -> principalDetails에서 속성과 메서드 만들어주고 사용
        // oAuth2User -> 반환형
        dto.setProvider(provider);
        dto.setProviderId(oAuth2UserInfo.getProviderId());
        return new PrincipalDetails(dto, oAuth2UserInfo.getAttributes()); // dto, attributes 전달

    }
}
