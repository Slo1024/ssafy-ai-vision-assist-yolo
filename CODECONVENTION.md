## Git commit message Convention

| 커밋 유형 | 의미 |
| --- | --- |
| `feat:`   | 새로운 기능 추가 |
| `fix:`  | 버그 수정 |
| `docs:`  | 문서 수정 |
| `style:`  | 코드 의미에 영향을 주지않는 수정.
ex) 세미콜론 누락, 빈 행 추가 |
| `refactor:`  | 코드 리팩토링 |
| `test:`  | 테스트 코드, 리팩토링 테스트 코드 추가 |
| `chore:`  | 패키지 매니저 수정, 디렉토리 생성, 개발환경 세팅
 ex) .gitignore |
| `design:`  | CSS 등 사용자 UI 디자인 변경 |
| `comment:`  | 필요한 주석 추가 및 변경 |
| `rename:`  | 파일 또는 폴더 명을 수정하거나 옮기는 작업만인 경우 |
| `remove:`  | 파일을 삭제하는 작업만 수행한 경우 |
| `!BREAKING CHANGE` | 커다란 API 변경의 경우 |
| `!HOTFIX` | 급하게 치명적인 버그를 고쳐야 하는 경우 |
| `merge` | 브랜치 병합 |

작성 예시

```bash
git commit -m "[BE] fix: 로그인 안되는 문제 수정중
(빈 행)
- 수정사항 1
- 수정사항 2
- 수정사항 3"
```

---

## Code Convention

## 백엔드
### 파일명

- **클래스/인터페이스 파일**
    - 클래스명과 동일하게 **`UpperCamelCase.java`**
    - 예: `UserService.java`, `OrderRepository.java`
- **열거형(Enum) 파일**
    - 마찬가지로 UpperCamelCase
    - 예: `UserRole.java`
- **테스트 클래스**
    - 테스트 대상 클래스명 + `Test` 접미사
    - 예: `UserServiceTest.java`
- **패키지명**
    - 모두 **소문자**, 단어는 점(`.`)으로 구분
    - 예: `com.example.myapp.service`

### 네이밍 컨벤션

- **클래스/인터페이스**: `UpperCamelCase`
- **메서드 / 변수**: `lowerCamelCase`
- **상수**: `UPPER_SNAKE_CASE`

### 중괄호 & 공백

- 중괄호 : K&R 스타일

```java
for(int i = 0; i < SOME_VALUE; i++){
    if(SOME_CONDITION){    
         SOME_STATMENT;
    }
    else{    
        SOME_STATMENT;
    }
}
```

- 공백 :  연산자 및 키워드 후 사용

```java
// 연산자 주변 공백
int sum = a + b * c;

// 키워드 뒤 공백
if (isActive) {
    count++;
} else {
    count = 0;
}
```

### 코드 형식 & 라인 처리

- **한 줄에 한 문장 (statement)** 원칙.
- **빈 라인**: 클래스 멤버 사이에 한 줄 공백.
- **라인 래핑**: 논리적 단위에 따라 줄 바꿈, 계속선은 추가 들여쓰기

```java
// 한 줄에 한 문장
int a = 10;

// 클래스 멤버 사이에 빈 줄
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}

// 라인 래핑 (긴 문장은 논리적 단위로 줄 바꿈, 계속선은 추가 들여쓰기)
String result = userRepository.findById(userId)
    .map(user -> user.getName() + " / " + user.getEmail())
    .orElse("Unknown");
```

### 주석

```java
// 한줄 주석

/*
 *여러 줄 주석
 */
```

## 프론트엔드
### 파일명

- **클래스/인터페이스/오브젝트**
    - 클래스명과 동일하게 **`UpperCamelCase.kt`**
    - 예: `MainActivity.kt`, `UserProfileFragment.kt`
- **Top-level 함수/유틸 함수만 있는 파일**
    - 관련 있는 이름을 붙이고 **UpperCamelCase** 또는 **소문자+언더스코어** 가능
    - 예: `NetworkUtils.kt`, `string_extensions.kt`
- **테스트 파일**
    - 대상 클래스명 + `Test.kt`
    - 예: `MainActivityTest.kt`
- **패키지명**
    - 모두 **소문자**, 언더스코어 사용하지 않음
    - 예: `com.example.myapp.ui.home`

### 네이밍 컨벤션

- **클래스/인터페이스**: `UpperCamelCase`
- **메서드 / 변수**: `lowerCamelCase`
- **상수**: `UPPER_SNAKE_CASE`

### 중괄호 & 공백

```kotlin
for (i in 0 until SOME_VALUE) {
    if (someCondition) {
        doSomething()
    } else {
        doAnotherThing()
    }
}

val sum = a + b * c
```

### 코드 형식 & 라인 처리

```kotlin
// 한 줄에 하나의 문장
val name = "Alice"
val age = 30

// 클래스 멤버 사이 빈 줄
class UserService {

    fun getUser(id: String): User? {
        return repository.findById(id)
    }

    fun saveUser(user: User) {
        repository.save(user)
    }
}

// 라인 래핑
val userInfo = userRepository.findById(userId)
    ?.let { user ->
        "${user.name} (${user.email})"
    }
    ?: "Unknown User"
```

### 주석

```kotlin
// 한줄 주석

/*
 *여러 줄 주석
 */
```