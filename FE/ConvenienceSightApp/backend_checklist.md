# 백엔드 팀과 확인해야 할 사항

## 1. API 인증 설정 확인
- [ ] `/api/v1/product/search` 엔드포인트가 인증을 요구하는가?
- [ ] 다른 API들 (`/api/v1/allergy`, `/api/v1/carts`)과 동일한 인증 방식을 사용하는가?
- [ ] 특별한 권한(Role)이 필요한가? (예: ROLE_USER, ROLE_ADMIN)

## 2. JWT 토큰 검증
- [ ] JWT 시크릿 키가 올바르게 설정되어 있는가?
- [ ] 토큰 검증 로직이 정상 작동하는가?
- [ ] 토큰 만료 시간 체크가 올바른가?

## 3. 서버 로그 확인
401 에러 발생 시 서버 로그에 나타나는 정확한 에러 메시지:
```
예시: "JWT signature does not match"
예시: "Token has expired"
예시: "No JWT token found in request headers"
```

## 4. Security Configuration
```java
// SecurityConfig.java에서 확인
.requestMatchers("/api/v1/product/**").authenticated() // 이 부분
```

## 5. Swagger 문서
- Swagger UI에서 해당 엔드포인트의 자물쇠 아이콘 🔒 확인
- Required Security 섹션 확인

## 6. 테스트 요청
백엔드 팀이 직접 테스트:
```bash
# 현재 클라이언트가 보내는 토큰으로 테스트
curl -X POST https://j13e101.p.ssafy.io/dev/api/v1/product/search \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyaHJsMzUzNUBnbWFpbC5jb20iLCJ1c2VySWQiOjMsImlhdCI6MTc1ODUzMTY1OCwiZXhwIjoxNzU4NjE4MDU4fQ.2iIc_KahjFIhHT5xlu08u9h2hixNUMl00eunIo52Sbk" \
  -F "shelf_images=@test.jpg"
```

## 7. 가능한 원인들
- [ ] 토큰은 유효하지만 해당 사용자(userId: 3)에게 권한이 없음
- [ ] 엔드포인트가 다른 인증 방식 요구 (Basic Auth, API Key 등)
- [ ] CORS 설정 문제
- [ ] 프록시/로드밸런서 설정 문제

## 8. 임시 해결방안
백엔드에서 임시로 해당 엔드포인트의 인증을 비활성화:
```java
.requestMatchers("/api/v1/product/search").permitAll() // 임시
```

## 결과 공유
위 사항들을 확인 후 결과를 공유해주세요:
- 정확한 에러 원인
- 필요한 수정 사항
- 예상 완료 시간