package com.project.lookey.OAuth.Service.Redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class JwtRedisService {
    private final RedisTemplate<String, String> redisTemplate;

    public JwtRedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveRefreshToken(String refreshToken, Integer userId, long expirationSeconds) {
        redisTemplate.opsForValue().set("refresh:" + userId, refreshToken, expirationSeconds, TimeUnit.SECONDS);
    }

    public String getRefreshToken(Integer userId) {
        return redisTemplate.opsForValue().get("refresh:" + userId);
    }

    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete("refresh:" + userId);
    }
}
