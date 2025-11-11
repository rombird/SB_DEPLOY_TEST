package com.example.demo.config.auth.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NaverUserInfo implements OAuth2UserInfo{ // 상속관계로 지
    // oAuth2User.getAttributes() 속성보고 확인
    // {resultcode=00, message=success, response={id=bBN9XYsi7G50NJxJ1hfdGLcoAPi0V7rOv7se2fyGmJs,
    //                                  profile_image=https://phinf.pstatic.net/contact/20250122_264/1737533417406tnV4I_JPEG/KakaoTalk_20250122_170705971.jpg,
    //                                  email=fhato5510@naver.com,
    //                                  name=임새롬}}
//    private String id;
//    private String profile_image;
//    private String email;
//    private String name;
    private Map<String, Object> response; // 위의 주석 코드한 속성들을 response로 해결가능 하기 때문에

    @Override
    public String getName() {
        return (String)response.get("name");
    }

    @Override
    public String getEmail() {
        return (String)response.get("email");
    }

    // implements OAuth2UserInfo
    @Override
    public String getProvider() {
        return "Naver";
    }

    @Override
    public String getProviderId() {
        return (String)response.get("id");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return response;
    }
}
