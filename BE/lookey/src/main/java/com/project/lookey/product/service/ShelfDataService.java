package com.project.lookey.product.service;

import com.project.lookey.product.dto.ShelfData;
import com.project.lookey.product.dto.ShelfDetectionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShelfDataService {

    @Qualifier("productRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String SHELF_DATA_KEY_PREFIX = "shelf_data:";
    private static final long TTL_MINUTES = 30;

    /**
     * 매대 데이터를 Redis에 저장
     * @param userId 사용자 ID
     * @param response AI 서버 응답 데이터
     */
    public void saveShelfData(Integer userId, ShelfDetectionResponse response) {
        try {
            String key = generateKey(userId);
            ShelfData shelfData = ShelfData.from(response, userId);

            // Redis에 저장 (TTL 30분)
            redisTemplate.opsForValue().set(key, shelfData, TTL_MINUTES, TimeUnit.MINUTES);

            log.info("매대 데이터 저장 완료 - userId: {}, 상품 개수: {}", userId, response.items().size());
        } catch (Exception e) {
            log.error("매대 데이터 저장 실패 - userId: {}", userId, e);
            throw new RuntimeException("매대 데이터 저장 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * Redis에서 매대 데이터 조회
     * @param userId 사용자 ID
     * @return 저장된 매대 데이터, 없으면 null
     */
    public ShelfData getShelfData(Integer userId) {
        try {
            String key = generateKey(userId);
            ShelfData shelfData = (ShelfData) redisTemplate.opsForValue().get(key);

            if (shelfData != null) {
                log.info("매대 데이터 조회 성공 - userId: {}, 상품 개수: {}", userId, shelfData.items().size());
            } else {
                log.info("매대 데이터 없음 - userId: {}", userId);
            }

            return shelfData;
        } catch (Exception e) {
            log.error("매대 데이터 조회 실패 - userId: {}", userId, e);
            return null;
        }
    }


    /**
     * Redis 키 생성
     * @param userId 사용자 ID
     * @return Redis 키
     */
    private String generateKey(Integer userId) {
        return SHELF_DATA_KEY_PREFIX + userId;
    }
}