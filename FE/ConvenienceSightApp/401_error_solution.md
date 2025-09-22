# 401 에러 최종 진단 및 해결방안

## 현재 상황
- **JWT 토큰이 정상적으로 전송됨**: `Bearer eyJhbGciOiJIUzI1NiJ9...`
- **토큰이 유효함**: exp가 2025년 9월 23일로 아직 만료되지 않음
- **그럼에도 401 Unauthorized 발생**

## 가능한 원인들

### 1. 서버 측 인증 설정 문제
**가장 가능성 높음**: 서버가 `/api/v1/product/search` 엔드포인트에 대해 다른 인증 방식을 요구하거나 인증을 제대로 처리하지 못함

### 2. 토큰 서명 검증 실패
서버의 시크릿 키가 변경되었거나 토큰 생성/검증 로직에 문제가 있을 수 있음

### 3. 권한 문제
사용자(userId: 3)에게 상품 검색 API 사용 권한이 없을 수 있음

## 즉시 시도할 해결책

### 1. 다른 인증 API로 테스트
```kotlin
// Repository.kt에 테스트 메서드 추가
suspend fun testAuth() {
    try {
        val response = api.getAllergies() // 또는 getCartList()
        Log.d("AuthTest", "Auth test result: ${response.isSuccessful}")
        if (!response.isSuccessful) {
            Log.e("AuthTest", "Failed: ${response.code()} - ${response.message()}")
        }
    } catch (e: Exception) {
        Log.e("AuthTest", "Exception", e)
    }
}
```

### 2. 서버 팀에 확인 사항
1. `/api/v1/product/search` 엔드포인트가 인증을 요구하는지
2. 특별한 권한이나 역할이 필요한지
3. 토큰 형식이 올바른지 (Bearer 접두사 포함)
4. 서버 로그에서 401 에러의 정확한 원인

### 3. 임시 우회 방법 (테스트용)
서버가 실제로 인증을 요구하지 않는다면:
```kotlin
// RetrofitClient.kt의 AuthInterceptor에서 특정 엔드포인트 제외
override fun intercept(chain: Interceptor.Chain): Response {
    val original = chain.request()

    // 상품 검색 API는 인증 헤더 제외 (테스트용)
    if (original.url.toString().contains("product/search")) {
        Log.d("AuthInterceptor", "Skipping auth for product search")
        return chain.proceed(original)
    }

    // 기존 로직...
}
```

### 4. Postman/curl로 직접 테스트
```bash
curl -X POST https://j13e101.p.ssafy.io/dev/api/v1/product/search \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: multipart/form-data" \
  -F "shelf_images=@test_image.jpg"
```

## 근본적 해결방안

### 백엔드 팀과 협의
1. **API 문서 확인**: Swagger나 API 문서에서 정확한 인증 요구사항 확인
2. **서버 로그 확인**: 401 에러가 발생하는 정확한 이유
3. **토큰 검증 로직 확인**: 서버의 JWT 검증 코드 검토

### 클라이언트 수정
1. **인증 방식 확인**: Basic Auth, API Key 등 다른 인증 방식이 필요한지 확인
2. **헤더 형식 확인**: `Bearer` 대신 다른 형식을 사용하는지 확인
3. **쿠키 기반 인증**: JSESSIONID 쿠키를 사용하는지 확인

## 결론
현재 클라이언트 코드는 정상적으로 작동하고 있으며, 유효한 JWT 토큰을 올바른 형식으로 전송하고 있습니다.
**서버 측 설정이나 권한 문제일 가능성이 가장 높으므로 백엔드 팀과 협의가 필요합니다.**