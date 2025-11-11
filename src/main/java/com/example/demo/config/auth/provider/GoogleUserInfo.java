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
public class GoogleUserInfo implements OAuth2UserInfo{
    // oAuth2User.getAttributes() : {sub=102036907562773250294, name=임새롬, given_name=새롬, family_name=임,
    //                              picture=https://lh3.googleusercontent.com/a/ACg8ocKiqzmuwcFb8lTicdMnMu6gqTinKnKuD_xyrUl-mrFt2Us4vg=s96-c,
    //                              email=romsae5510@gmail.com,
    //                              email_verified=true}
    private Map<String, Object> attributes;

    // implements 상속 -> 메서드 구현
    @Override
    public String getName() {
        return (String)attributes.get("given_name");
    }

    @Override
    public String getEmail() {
        return (String)attributes.get("email");
    }

    @Override
    public String getProvider() {
        return "Google";
    }

    @Override
    public String getProviderId() {
        return (String)attributes.get("sub");
    }
}
