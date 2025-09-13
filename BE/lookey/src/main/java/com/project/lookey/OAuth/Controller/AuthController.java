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
        System.out.println("[DEBUG] Authorization: " + authorizationHeader);

        if (!authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", "Authorization 헤더 형식이 잘못되었습니다."
            ));
        }

        String idToken = authorizationHeader.substring(7);
        GoogleIdToken.Payload payload;

        try {
            payload = googleVerifierService.verify(idToken);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "status", 500,
                    "message", "Google OAuth 검증 실패: " + e.getMessage()
            ));
        }

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        System.out.println("[DEBUG] payload email: " + email + ", name: " + name);

        // 유저가 없으면 db에 저장
        User user;
        try {
            user = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.save(User.builder()
                            .email(email)
                            .name(name)
                            .build()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "status", 500,
                    "message", "DB 저장 실패: " + e.getMessage()
            ));
        }

        String jwt = jwtProvider.createToken(user.getId(), user.getEmail());
        System.out.println("[DEBUG] JWT: " + jwt + ", userId: " + user.getId());

        //jwtRedisService.saveToken(jwt, user.getId().longValue(), 7 * 24 * 60 * 60L);

        Map<String, Object> data = Map.of(
                "jwtToken", jwt,
                "userId", user.getId()
        );

        Map<String, Object> response = Map.of(
                "message", "로그인 성공",
                "data", data
        );

        return ResponseEntity.ok(response);
    }
}
