package com.project.lookey.OAuth.Service.oauth;

import com.project.lookey.OAuth.Entity.User;
import com.project.lookey.OAuth.Repository.UserRepository;
import lombok.RequiredArgsConstructor;import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .name(name)
                            .build();
                    return userRepository.save(newUser);
                });

        return new CustomOAuth2User(
                user.getId(),
                oAuth2User.getAttributes(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}

