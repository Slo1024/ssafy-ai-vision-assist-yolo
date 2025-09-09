package com.project.lookey.OAuth.Service.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleVerifierService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleVerifierService() {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList("95484213731-5qj9f0guuquq6pprklb8mtvfr41re2i2.apps.googleusercontent.com"))
                .build();
    }

    public GoogleIdToken.Payload verify(String idTokenString) {
        try {
            System.out.println("검증 시도 중인 토큰: " + idTokenString);
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                System.out.println("유효하지 않은 ID Token입니다.");
                throw new RuntimeException("유효하지 않은 ID Token입니다.");
            }

            return idToken.getPayload();
        } catch (Exception e) {
            System.out.println("ID Token 검증 중 오류 발생" + e);
            throw new RuntimeException("ID Token 검증 중 오류 발생", e);
        }
    }
}

