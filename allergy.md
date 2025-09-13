# 알레르기 API 구현 가이드

## 📋 개요
Cart API와 동일한 구조로 구현된 알레르기 관리 API입니다. 사용자가 자신의 알레르기 정보를 관리할 수 있습니다.

## 🏗️ 아키텍처

### 엔티티 관계
```
User (사용자) ←→ Allergy (사용자 알레르기) ←→ AllergyList (알레르기 마스터 데이터)
```

## 📁 파일 구조
```
allergy/
├── entity/
│   ├── Allergy.java          - 사용자-알레르기 연결 테이블
│   └── AllergyList.java      - 알레르기 마스터 데이터
├── dto/
│   ├── AllergyAddRequest.java     - 알레르기 추가 요청
│   ├── AllergyRemoveRequest.java  - 알레르기 삭제 요청
│   ├── AllergyListResponse.java   - 내 알레르기 목록 응답
│   └── AllergySearchResponse.java - 알레르기 검색 응답
├── repository/
│   ├── AllergyRepository.java     - 사용자 알레르기 데이터 접근
│   └── AllergyListRepository.java - 알레르기 마스터 데이터 접근
├── service/
│   └── AllergyService.java        - 비즈니스 로직
└── controller/
    └── AllergyController.java     - REST API 엔드포인트
```

## 🛠️ 주요 어노테이션 설명

### Entity 어노테이션

#### `@Entity`
- JPA 엔티티 클래스로 지정
- 데이터베이스 테이블과 매핑되는 클래스

#### `@Table`
```java
@Table(
    name = "allergy",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UQ_ALLERGY_USER_ALLERGYLIST",
            columnNames = {"user_id", "allergy_id"}
        )
    }
)
```
- `name`: 실제 데이터베이스 테이블 이름 지정
- `uniqueConstraints`: 복합 유니크 제약조건 (한 사용자가 같은 알레르기를 중복 등록 방지)

#### `@ManyToOne`
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;
```
- 다대일 관계 설정 (여러 알레르기 → 한 사용자)
- `FetchType.LAZY`: 지연 로딩 (필요할 때만 데이터 조회)
- `@JoinColumn`: 외래키 컬럼 이름 지정

#### `@CreationTimestamp`
```java
@CreationTimestamp
@Column(name = "created_at", updatable = false, nullable = false)
private LocalDateTime createdAt;
```
- 엔티티 생성 시 자동으로 현재 시간 설정
- `updatable = false`: 수정 불가능
- `nullable = false`: NULL 값 불허

### Repository 어노테이션

#### `@Query`
```java
@Query("""
    SELECT new com.project.lookey.allergy.dto.AllergyListResponse$Item(
        a.id, 
        a.allergyList.id, 
        a.allergyList.name
    )
    FROM Allergy a 
    WHERE a.user.id = :userId
    ORDER BY a.createdAt DESC
""")
```
- JPQL(Java Persistence Query Language) 쿼리 작성
- `new 생성자()`: DTO 생성자 직접 호출로 결과 매핑
- `:userId`: 파라미터 바인딩

#### `@Modifying`
```java
@Modifying
@Query("DELETE FROM Allergy a WHERE a.user.id = :userId AND a.allergyList.id = :allergyListId")
```
- 데이터 수정/삭제 쿼리임을 명시
- `@Transactional`과 함께 사용해야 함

### Service 어노테이션

#### `@Service`
- Spring의 서비스 레이어 컴포넌트로 등록
- 비즈니스 로직을 담당하는 클래스

#### `@Transactional`
```java
@Transactional(readOnly = true)  // 클래스 레벨: 조회 전용
@Transactional                   // 메소드 레벨: 수정 가능
```
- 데이터베이스 트랜잭션 관리
- `readOnly = true`: 성능 최적화 (조회 전용)

#### `@RequiredArgsConstructor`
- Lombok: final 필드에 대한 생성자 자동 생성
- 의존성 주입용 생성자 생성

### Controller 어노테이션

#### `@RestController`
- `@Controller` + `@ResponseBody`
- REST API 컨트롤러로 지정, JSON 응답 자동 변환

#### `@RequestMapping("/api/v1/allergies")`
- 클래스 레벨 URL 매핑
- 모든 메소드의 기본 경로 설정

#### `@Tag(name = "Allergy", description = "알레르기 관련 API")`
- Swagger/OpenAPI 문서화용
- API 그룹화 및 설명 추가

#### HTTP 메소드 어노테이션
```java
@GetMapping                           // 조회
@PostMapping                          // 생성
@DeleteMapping(consumes = "application/json")  // 삭제
@GetMapping("/search/{searchword}")   // 경로 변수 포함 조회
```

#### `@AuthenticationPrincipal(expression = "id")`
- Spring Security에서 인증된 사용자 정보 추출
- JWT 토큰에서 사용자 ID 자동 추출

#### `@Valid @RequestBody`
- 요청 본문을 DTO로 자동 변환
- `@Valid`: DTO 검증 어노테이션 적용

#### `@PathVariable("searchword")`
- URL 경로에서 변수 값 추출
- `/search/견과류` → searchword = "견과류"

## 🔗 API 엔드포인트

### 1. 내 알레르기 목록 조회
```
GET /api/v1/allergies
```
- 인증된 사용자의 알레르기 목록 반환
- 생성일시 기준 내림차순 정렬

### 2. 알레르기 추가
```
POST /api/v1/allergies
Content-Type: application/json

{
  "allergyId": 1
}
```
- 사용자에게 새로운 알레르기 추가
- 중복 등록 시 409 Conflict 응답

### 3. 알레르기 삭제
```
DELETE /api/v1/allergies
Content-Type: application/json

{
  "allergyId": 1
}
```
- 사용자의 특정 알레르기 제거
- 존재하지 않을 시 404 Not Found 응답

### 4. 알레르기 검색
```
GET /api/v1/allergies/search/{keyword}
```
- 키워드로 사용 가능한 알레르기 목록 검색
- 알레르기 이름에 키워드 포함된 항목 반환

## 🎯 주요 특징

### Cart API와의 유사점
- 동일한 RESTful API 구조
- 사용자 인증 기반 CRUD 작업
- 중복 방지 로직
- 동일한 응답 형식 (status, message, result)

### 차이점
- Cart: User ↔ Product 관계
- Allergy: User ↔ AllergyList 관계
- 도메인별 비즈니스 로직 및 메시지

### 보안 및 예외 처리
- JWT 기반 사용자 인증
- 데이터베이스 제약조건 위반 시 적절한 HTTP 상태코드 반환
- 트랜잭션 관리로 데이터 일관성 보장