package com.project.lookey.path.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PathException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String code;
    private final String info;

    public PathException(HttpStatus httpStatus, String message, String code, String info) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
        this.info = info;
    }

    // 자주 사용되는 예외들을 위한 정적 메서드
    public static PathException invalidParameter(String paramName) {
        return new PathException(
            HttpStatus.BAD_REQUEST,
            "잘못된 요청입니다. 위도(lat), 경도(lng)는 필수입니다.",
            "INVALID_PARAMETER",
            "Missing or invalid parameter: " + paramName
        );
    }

    public static PathException noResults(int radius) {
        return new PathException(
            HttpStatus.NOT_FOUND,
            "근처 편의점을 찾을 수 없습니다.",
            "NO_RESULTS",
            "0 places found within " + radius + "m radius"
        );
    }

    public static PathException rateLimitExceeded() {
        return new PathException(
            HttpStatus.TOO_MANY_REQUESTS,
            "요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.",
            "RATE_LIMIT_EXCEEDED",
            "Max 60 requests per minute"
        );
    }

    public static PathException kakaoApiError(String reason) {
        return new PathException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            "KAKAO_API_ERROR",
            reason
        );
    }
}