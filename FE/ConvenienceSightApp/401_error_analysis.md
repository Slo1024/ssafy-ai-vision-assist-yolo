# 상품 인식 API 401 에러 분석 및 해결방안

## 문제 상황
상품 인식 API 호출 시 RetrofitClient에서 지속적으로 401(Unauthorized) 에러가 발생하고 있습니다.

## 원인 분석

### 1. 인증 토큰 문제
**RetrofitClient.kt:27-32** 에서 토큰 획득 과정을 확인했을 때:
```kotlin
var token = TokenProvider.token
if (token.isNullOrEmpty()) {
    token = PrefUtil.getJwtToken(AppCtx.app)
    if (!token.isNullOrEmpty()) TokenProvider.token = token
}
if (!token.isNullOrEmpty()) builder.header("Authorization", "Bearer $token")
```

**가능한 문제점들:**
- **토큰 만료**: JWT 토큰이 만료되었지만 갱신되지 않았을 가능성
- **토큰 저장 실패**: `PrefUtil.getJwtToken()`에서 복호화 실패로 null 반환 (PrefUtil.kt:66)
- **인증되지 않은 상태**: 로그인 과정에서 토큰이 제대로 저장되지 않았을 가능성

### 2. API 엔드포인트 인증 요구사항
**ApiService.kt**에서 상품 인식 관련 API들:
- `searchShelf()` (line 34-37): 선반 이미지 분석
- `searchProductLocationJson()` (line 40-44): 상품 위치 검색 (JSON)
- `searchProductLocation()` (line 47-52): 상품 위치 검색 (멀티파트)

이 API들은 모두 인증이 필요한 엔드포인트로 보이며, 유효한 JWT 토큰 없이는 401 에러를 반환합니다.

### 3. 토큰 갱신 메커니즘 문제
**RetrofitClient.kt:44-71**의 `TokenAuthenticator`에서:
- 토큰 갱신 시도 중 실패할 경우 `logoutAndNull()` 호출
- 무한루프 방지를 위해 재시도를 1회로 제한
- 갱신 실패 시 모든 저장된 인증 정보 삭제

## 해결방안

### 1. 즉시 해결방안

#### A. 로그인 상태 확인
```kotlin
// 디버그용 로깅 추가
Log.d("Auth", "Current token: ${TokenProvider.token}")
Log.d("Auth", "Stored JWT: ${PrefUtil.getJwtToken(AppCtx.app)}")
Log.d("Auth", "Stored Refresh: ${PrefUtil.getRefreshToken(AppCtx.app)}")
```

#### B. 강제 재로그인
```kotlin
// 상품 인식 API 호출 전 토큰 유효성 검사
if (TokenProvider.token.isNullOrEmpty() && PrefUtil.getJwtToken(AppCtx.app).isNullOrEmpty()) {
    // 로그인 화면으로 리다이렉트
    authListener?.onLogout()
    return
}
```

### 2. 근본적 해결방안

#### A. 토큰 갱신 로직 개선
**RetrofitClient.kt**의 `TokenAuthenticator` 수정:
```kotlin
// 토큰 갱신 실패 시 더 상세한 로깅
Log.e("TokenAuthenticator", "Refresh failed - HTTP ${resp.code()}: ${resp.message()}")
```

#### B. API 호출 전 토큰 검증 추가
**Repository.kt**에 토큰 검증 로직 추가:
```kotlin
private suspend fun ensureValidToken() {
    val token = TokenProvider.token ?: PrefUtil.getJwtToken(AppCtx.app)
    if (token.isNullOrEmpty()) {
        throw IllegalStateException("No authentication token available")
    }
}

suspend fun productShelfSearch(cacheDir: File, frame: Bitmap): ApiResponse<ShelfSearchResult> {
    ensureValidToken() // 토큰 검증 추가
    val part = buildShelfImagePart(cacheDir, frame)
    return api.searchShelf(part).bodyOrThrow()
}
```

#### C. EncryptedSharedPreferences 안정성 개선
**PrefUtil.kt**는 이미 복호화 실패 시 재생성 로직이 구현되어 있으나, 추가 개선 가능:
```kotlin
// 토큰 저장 시 검증 로직 추가
fun saveJwtTokenWithValidation(context: Context, token: String): Boolean {
    return try {
        saveJwtToken(context, token)
        getJwtToken(context) == token // 저장 후 검증
    } catch (e: Exception) {
        Log.e("PrefUtil", "Token save/validation failed", e)
        false
    }
}
```

### 3. 디버깅 단계

1. **로그 확인**: HTTP 요청/응답 로그에서 실제 Authorization 헤더 확인
2. **토큰 상태 확인**: 앱 시작 시 저장된 토큰들의 상태 로깅
3. **서버 응답 확인**: 401 응답의 상세 에러 메시지 확인
4. **토큰 갱신 테스트**: 수동으로 refresh token API 호출 테스트

### 4. 예방책

- **토큰 만료 시간 모니터링**: JWT 토큰의 exp 클레임 확인
- **자동 토큰 갱신**: API 호출 전 토큰 만료 시간 체크 후 사전 갱신
- **에러 처리 개선**: 401 에러 시 사용자에게 명확한 안내 메시지 제공

## 결론

401 에러의 주요 원인은 **토큰 부재 또는 만료**입니다. 우선 로그인 상태를 확인하고, 필요시 재로그인을 진행한 후 상품 인식 API를 호출하시기 바랍니다. 장기적으로는 토큰 검증 및 갱신 로직을 강화하여 사용자 경험을 개선할 수 있습니다.