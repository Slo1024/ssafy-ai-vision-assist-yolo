# 🚀 LOOKEY 프로젝트 포팅 메뉴얼

## 📋 목차
1. [개요](#개요)
2. [시스템 요구사항](#시스템-요구사항)
3. [환경 설정](#환경-설정)
4. [빌드 및 배포](#빌드-및-배포)
5. [외부 서비스 설정](#외부-서비스-설정)
6. [DB 및 설정 파일](#db-및-설정-파일)

---

## 📖 개요

**LOOKEY**는 AI 기반 편의점 상품 인식 및 추천 서비스입니다.
- **Backend**: Spring Boot 3.5.5, Java 17, MySQL 8.0, Redis 7
- **Frontend**: Android App (Kotlin/Gradle)
- **AI**: FastAPI, PyTorch, YOLO (Ultralytics)
- **Infra**: Docker Compose, Jenkins CI/CD

---

## 💻 시스템 요구사항

### 1. 필수 소프트웨어
```bash
# Java 17 (OpenJDK 권장)
java -version
# java 17.0.x

# Docker & Docker Compose
docker --version
docker-compose --version

# Git
git --version

# Python 3.8+ (AI 서비스)
python --version
# Python 3.8+

# Node.js 18+ (선택사항, 프론트엔드 도구)
node --version
```

### 2. 권장 IDE/도구
- **Backend**: IntelliJ IDEA / VS Code
- **Android**: Android Studio Arctic Fox 이상
- **AI**: VS Code / PyCharm

### 3. 서버 포트 사용 현황
- `8081`: Backend (Production)
- `8082`: Backend (Development)
- `8083`: AI Service
- `3306`: MySQL
- `6379`: Redis

---

## ⚙️ 환경 설정

### 1. GitLab 클론 후 디렉토리 구조 확인
```bash
git clone https://lab.ssafy.com/s13-ai-image-sub1/S13P21E101.git
cd S13P21E101

# 디렉토리 구조
├── BE/lookey/          # Spring Boot 백엔드
├── FE/ConvenienceSightApp/  # Android 앱
├── AI/                 # FastAPI AI 서비스
├── docker-compose.*.yml
└── Jenkinsfile
```

### 2. 환경 변수 설정 (.env)
**프로젝트 루트에 `.env` 파일 생성:**

```bash
# MySQL Configuration
MYSQL_ROOT_PASSWORD=your_mysql_root_password
MYSQL_DATABASE=lookey
MYSQL_USER=lookey
MYSQL_PASSWORD=your_mysql_user_password

# Spring Boot Database Configuration
SPRING_DATASOURCE_URL=jdbc:mysql://mysql-shared:3306/lookey?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=lookey
SPRING_DATASOURCE_PASSWORD=your_mysql_user_password
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver

# JPA Configuration
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.MySQLDialect
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false

# Redis Configuration
SPRING_DATA_REDIS_HOST=redis-shared
SPRING_DATA_REDIS_PORT=6379

# Server Configuration
SERVER_PORT=8080

# Google OAuth Configuration
GOOGLE_OAUTH_ID=your_google_oauth_client_id
GOOGLE_OAUTH_SECRET=your_google_oauth_client_secret
GOOGLE_REDIRECT_URI=http://localhost:8081/login/oauth2/code/google
GOOGLE_CALLBACK_URL=http://localhost:8081/auth/google/callback

# JWT Configuration
JWT_SECRET=your_jwt_secret_key_base64_encoded

# Google Cloud Vision API Configuration
GOOGLE_APPLICATION_CREDENTIALS=./your_google_service_account_key.json
GOOGLE_CLOUD_PROJECT_ID=your_google_cloud_project_id

# AI Service Configuration
AI_SERVICE_URL=http://localhost:8083/

# External API Keys
KAKAO_REST_KEY=your_kakao_rest_api_key
API_KEY=your_haccp_api_key
```

### 3. Google Cloud 서비스 계정 키 파일
Google Cloud에서 다운받은 서비스 계정 키 파일(JSON)을 프로젝트 루트에 배치하고, `.env` 파일의 `GOOGLE_APPLICATION_CREDENTIALS`에 파일명을 설정합니다.

---

## 🔨 빌드 및 배포

### 1. 로컬 개발 환경 실행

#### Backend 단독 실행
```bash
cd BE/lookey
chmod +x ./gradlew
./gradlew clean build -x test
./gradlew bootRun
```

#### Docker Compose 이용 (권장)
```bash
# 개발 환경
docker-compose -f docker-compose.dev.yml up --build

# 프로덕션 환경
docker-compose -f docker-compose.prod.yml up --build

# AI 서비스 별도 실행
docker-compose -f docker-compose.ai.yml up --build
```

### 2. AI 서비스 단독 실행
```bash
cd AI
pip install -r requirements.txt
python main.py
```

### 3. Android 앱 빌드
```bash
cd FE/ConvenienceSightApp
./gradlew assembleDebug
# APK 위치: app/build/outputs/apk/debug/
```

### 4. 배포 시 특이사항

#### Jenkins CI/CD 파이프라인
- **master 브랜치**: 프로덕션 배포 (포트 8081)
- **dev 브랜치**: 개발 배포 (포트 8082)
- 자동 빌드 및 배포 (Webhook 연동)

#### 배포 후 Health Check
```bash
# Backend Health Check
curl http://localhost:8081/actuator/health

# AI Service Health Check
curl http://localhost:8083/health
```

---

## 🌐 외부 서비스 설정

### 1. Google OAuth 2.0 설정
**Google Cloud Console** (https://console.cloud.google.com)

1. 새 프로젝트 생성 또는 기존 프로젝트 선택
2. `APIs & Services` > `Credentials` 이동
3. OAuth 2.0 클라이언트 ID 생성:
   - **애플리케이션 유형**: 웹 애플리케이션
   - **승인된 리디렉션 URI**: `http://localhost:8081/login/oauth2/code/google`
   - 생성된 **클라이언트 ID**와 **클라이언트 시크릿**을 `.env`에 설정

### 2. Google Cloud Vision API
1. Google Cloud Console에서 `Vision API` 활성화
2. 서비스 계정 생성 및 키 파일 다운로드
3. 다운받은 JSON 키 파일을 프로젝트 루트에 배치
4. 파일명을 `.env`의 `GOOGLE_APPLICATION_CREDENTIALS`에 설정

### 3. KAKAO REST API
**KAKAO Developers** (https://developers.kakao.com)

1. 내 애플리케이션 > 애플리케이션 추가하기
2. 앱 정보 입력 후 생성
3. **앱 키** > **REST API 키** 복사하여 `.env`에 설정
4. **플랫폼** > **Web 플랫폼 등록** > 도메인 추가 (`http://localhost:8081`)

### 4. 공공데이터포털 HACCP API
**공공데이터포털** (https://www.data.go.kr)

1. 회원가입 및 로그인
2. **HACCP 인증정보 서비스** 검색 후 활용신청
3. 승인 후 발급받은 **인증키**를 `.env`의 `API_KEY`에 설정

---

## 💾 DB 및 설정 파일

### 1. 데이터베이스 정보
- **DB 종류**: MySQL 8.0
- **데이터베이스명**: `lookey`
- **계정**: `lookey` / `your_mysql_user_password`
- **포트**: `3306`
- **문자셋**: `utf8mb4_unicode_ci`

### 2. 주요 설정 파일 위치

#### Backend 설정 파일
```
BE/lookey/src/main/resources/
├── application.properties      # 메인 설정 파일
└── static/                    # 정적 리소스
```

#### 환경 변수 파일
```
프로젝트루트/
├── .env                       # 환경 변수
├── your_google_service_account_key.json  # Google 서비스 계정 키
└── clip_linear_head.pt       # AI 모델 파일
```

### 3. 빌드 설정 파일
- `BE/lookey/build.gradle`: Backend 의존성 및 빌드 설정
- `AI/requirements.txt`: AI 서비스 Python 패키지
- `FE/ConvenienceSightApp/build.gradle`: Android 앱 빌드 설정

### 4. Docker 설정 파일
- `docker-compose.prod.yml`: 프로덕션 환경
- `docker-compose.dev.yml`: 개발 환경
- `docker-compose.ai.yml`: AI 서비스

### 5. DB 덤프 파일 (최신본)
- **파일명**: `lookey_db_dump.sql`
- **생성일**: 2025년 9월 26일
- **크기**: 61KB
- **포함 테이블**:
  - `users`: 사용자 정보
  - `user`: 사용자 관련 테이블
  - `product`: 상품 정보
  - `cart`: 장바구니
  - `allergy`: 알레르기 정보
  - `allergy_list`: 알레르기 목록
  - `product_allergy`: 상품-알레르기 연관 테이블

#### DB 복원 방법
```bash
# Docker MySQL 컨테이너에 덤프 파일 복원
docker exec -i mysql-shared mysql -u root -pyour_mysql_root_password lookey < lookey_db_dump.sql

# 또는 직접 MySQL 서버에 복원
mysql -u root -p lookey < lookey_db_dump.sql
```

---

## 🔍 트러블슈팅

### 1. Docker 컨테이너 실행 시 오류
```bash
# 컨테이너 로그 확인
docker logs springapp-prod
docker logs lookey-ai-service

# 컨테이너 재시작
docker-compose -f docker-compose.prod.yml restart
```

### 2. DB 연결 오류
- MySQL 서비스 상태 확인
- 환경 변수 `SPRING_DATASOURCE_URL` 확인
- 방화벽 3306 포트 개방 확인

### 3. 외부 API 호출 오류
- API 키 유효성 확인
- 네트워크 연결 상태 확인
- 일일 호출 제한량 확인

---

## 📞 문의

프로젝트 관련 문의사항은 개발팀에게 연락 바랍니다.

**서버 접속 정보**:
- 호스트: `j13e101.p.ssafy.io`
- 키 파일: `J13E101T.pem`