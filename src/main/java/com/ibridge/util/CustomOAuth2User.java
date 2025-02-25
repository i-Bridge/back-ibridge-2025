package com.ibridge.util;

import lombok.Getter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

public class CustomOAuth2User extends User implements OAuth2User {

    private final Map<String, Object> attributes;
    @Getter
    private final String email;

    public CustomOAuth2User(Map<String, Object> attributes, String email) {
        super(email, "", AuthorityUtils.NO_AUTHORITIES);
        this.attributes = attributes;
        this.email = email;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return email;  // 사용자의 이름을 이메일로 설정
    }

}
