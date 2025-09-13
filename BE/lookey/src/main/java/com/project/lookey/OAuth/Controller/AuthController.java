package com.project.lookey.OAuth.Controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.project.lookey.Common.ResponseUtil;
import com.project.lookey.OAuth.Entity.User;
import com.project.lookey.OAuth.Repository.UserRepository;
import com.project.lookey.OAuth.Service.Redis.JwtRedisService;
import com.project.lookey.OAuth.Service.google.GoogleVerifierService;
import com.project.lookey.OAuth.Service.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GoogleVerifierService googleVerifierService;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final JwtRedisService jwtRedisService;

    @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> loginWithGoogle(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        if (!authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "Authorization 헤더 형식이 잘못되었습니다."
            ));
        }

        String idToken = authorizationHeader.substring(7);
        GoogleIdToken.Payload payload = googleVerifierService.verify(idToken);

        String email = payload.getEmail();
        String name = (String) payload.get("name");

        // 유저가 없으면 db에 저장
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = userRepository.save(User.builder()
                            .email(email)
                            .name(name)
                            .build());
                    userRepository.flush(); // ID 보장
                    return newUser;
                });

        String jwt = jwtProvider.createToken(user.getId(), user.getEmail());

        //jwtRedisService.saveToken(jwt, user.getId().longValue(), 7 * 24 * 60 * 60L);

        Map<String, Object> body = new HashMap<>();
        body.put("jwtToken", jwt);
        body.put("userId", user.getId());

        return ResponseUtil.ok("로그인 성공", body);
    }
}
