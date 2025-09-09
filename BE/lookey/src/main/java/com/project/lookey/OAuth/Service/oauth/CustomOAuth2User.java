package com.project.lookey.OAuth.Service.oauth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private Integer userId;
    private Map<String, Object> attributes;
    private Collection<? extends GrantedAuthority> authorities;

    public CustomOAuth2User(Integer userId,
                            Map<String, Object> attributes,
                            Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.attributes = attributes;
        this.authorities = authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        Object idAttr = attributes.get("sub"); // 구글 고유 id
        return idAttr != null ? idAttr.toString() : "";
    }

    public Integer getUserId() {
        return userId;
    }
}


