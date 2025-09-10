package com.project.lookey.OAuth.Service.Redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JwtRedisService {

    private final RedisTemplate<String, String> redisTemplate;

    // JWT를 Redis에 저장 (만료시간 설정 가능)
    public void saveToken(String jwt, Long userId, long expireSeconds) {
        redisTemplate.opsForValue().set(jwt, userId.toString(), expireSeconds, TimeUnit.SECONDS);
    }

    // JWT가 Redis에 있는지 확인 (블랙리스트용)
    public boolean existsToken(String jwt) {
        return redisTemplate.hasKey(jwt);
    }

    // JWT 삭제 (로그아웃 등)
    public void deleteToken(String jwt) {
        redisTemplate.delete(jwt);
    }
}

