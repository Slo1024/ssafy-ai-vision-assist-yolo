# 🚀 LOOKEY API 명세서

AI 기반 편의점 상품 인식 및 추천 서비스 **LOOKEY**의 전체 API 명세서입니다.

## 📋 목차
1. [인증 API](#인증-api)
2. [사용자 알레르기 API](#사용자-알레르기-api)
3. [장바구니 API](#장바구니-api)
4. [상품 검색 및 인식 API](#상품-검색-및-인식-api)
5. [지도/위치 API](#지도위치-api)
6. [이미지 분석 API](#이미지-분석-api)
7. [AI 서비스 API](#ai-서비스-api)

---

## 🔐 인증 API

### Google OAuth 로그인
**POST** `/api/auth/google`

Google OAuth 토큰을 사용한 로그인 처리

#### Request Headers
| Name | Type | Description | Required |
|------|------|-------------|----------|
| Authorization | String | Bearer {google_id_token} | ✅ |
| Content-Type | String | application/json | ✅ |

#### Response
```json
{
  "message": "로그인 성공",
  "data": {
    "jwtToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 123
  }
}
```

### JWT 토큰 갱신
**POST** `/api/auth/refresh`

Refresh Token을 사용한 Access Token 갱신

#### Request Headers
| Name | Type | Description | Required |
|------|------|-------------|----------|
| Authorization | String | Bearer {refresh_token} | ✅ |

#### Response
```json
{
  "jwtToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## 🚫 사용자 알레르기 API

### 알레르기 목록 조회
**GET** `/api/v1/allergy`

사용자의 등록된 알레르기 목록 조회

#### Request Headers
| Name | Type | Description | Required |
|------|------|-------------|----------|
| Authorization | String | Bearer {jwt_token} | ✅ |

#### Response
```json
{
  "status": 200,
  "message": "알레르기 목록 조회 성공",
  "result": {
    "names": [
      {
        "allergy_id": 1,
        "name": "우유"
      },
      {
        "allergy_id": 3,
        "name": "땅콩"
      }
    ]
  }
}
```

### 알레르기 검색
**GET** `/api/v1/allergy/search/{searchword}`

알레르기 명 검색

#### Request Headers
| Name | Type | Description | Required |
|------|------|-------------|----------|
| Authorization | String | Bearer {jwt_token} | ✅ |

#### Path Parameters
| Name | Type | Description | Required |
|------|------|-------------|----------|
| searchword | String | 검색할 알레르기명 | ✅ |

#### Response
```json
{
  "status": 200,
  "message": "알레르기 검색 성공",
  "result": [
    {
      "allergy_name": "소고기",
      "allergy_id": "2"
    },
    {
      "allergy_name": "돼지고기",
      "allergy_id": "3"
    }
  ]
}
```

### 알레르기 등록
**POST** `/api/v1/allergy`

사용자 알레르기 정보 등록

#### Request Headers
| Name | Type | Description | Required |
|------|------|-------------|----------|
| Authorization | String | Bearer {jwt_token} | ✅ |
| Content-Type | String | application/json | ✅ |

#### Request Body
```json
{
  "allergy_id": 1
}
```

#### Response
```json
{
  "status": 201,
  "message": "알레르기를 등록했습니다.",
  "result": {}
}
```

### 알레르기 삭제
**DELETE** `/api/v1/allergy`

사용자 알레르기 정보 삭제

#### Request Headers
| Name | Type | Description | Required |
|------|------|-------------|----------|
| Authorization | String | Bearer {jwt_token} | ✅ |
| Content-Type | String | application/json | ✅ |

#### Request Body
```json
{
  "allergy_id": 1
}
```

#### Response
```json
{
  "status": 200,
  "message": "알레르기를 삭제했습니다.",
  "result": {}
}
```

---

## 🛒 장바구니 API

### 장바구니 목록 조회
**GET** `/api/v1/carts`

사용자의 장바구니 상품 목록 조회

#### Request Headers
| Name | Type | Description | Required |
|------|------|-------------|----------|
| Authorization | String | Bearer {jwt_token} | ✅ |

#### Response
```json
{
  "status": 200,
  "message": "장바구니 목록 조회 성공",
  "result": {
    "items": [
      {
        "cart_id": 1,
        "product_id": 1,
        "product_name": "코카콜라"
      },
      {
        "cart_id": 2,
        "product_id": 5,
        "product_name": "포카칩"
      }
    ]
  }
}
```

### 상품 검색
**GET** `/api/v1/carts/search/{searchword}`

상품명으로 상품 검색

#### Request Headers
| Name | Type | Description | Required |
|------|------|-------------|----------|
| Authorization | String | Bearer {jwt_token} | ✅ |

#### Path Parameters
| Name | Type | Description | Required |
|------|------|-------------|----------|
| searchword | String | 검색할 상품명 | ✅ |

#### Response
```json
{
  "status": 200,
  "message": "검색 성공",
  "result": {
    "items": [
      {
        "product_id": 1,
        "product_name": "코카콜라"
      },
      {
        "product_id": 5,
        "product_name": "코카콜라제로"
      }
    ]
  }
}
```

### 장바구니 상품 추가
**POST** `/api/v1/carts`

장바구니에 상품 추가

#### Request Headers
| Name | Type | Description | Required |
|------|------|-------------|----------|
| Authorization | String | Bearer {jwt_token} | ✅ |
| Content-Type | String | application/json | ✅ |

#### Request Body
```json
{
  "product_id": 1
}
```

#### Response
```json
{
  "status": 201,
  "message": "장바구니에 상품을 담았습니다.",
  "result": {}
}
```

### 장바구니 상품 삭제
**DELETE** `/api/v1/carts`

장바구니에서 상품 삭제

#### Request Headers
| Name | Type | Description | Required |
|------|------|-------------|----------|
| Authorization | String | Bearer {jwt_token} | ✅ |
| Content-Type | String | application/json | ✅ |

#### Request Body
```json
{
  "cart_id": 1
}
```

#### Response
```json
{
  "status": 200,
  "message": "장바구니에서 삭제하였습니다.",
  "result": null
}
```

---

## 📷 상품 검색 및 인식 API

### 매대 이미지에서 장바구니 상품 매칭
**POST** `/api/v1/product/search`

매대 이미지를 업로드하여 장바구니 상품과 매칭되는 상품들을 찾아주는 API

#### Request Headers
| Name | Type | Description | Required |
|------|------|-------------|----------|
| Authorization | String | Bearer {jwt_token} | ✅ |
| Content-Type | String | multipart/form-data | ✅ |

#### Request Body (Form Data)
| Name | Type | Description | Required |
|------|------|-------------|----------|
| file | File | 매대 이미지 (JPEG 형식만 지원) | ✅ |

#### Response
```json
{
  "status": 200,
  "message": "매대에서 장바구니 상품 확인 완료",
  "result": {
    "count": 2,
    "matched_products": [
      "코카콜라",
      "스프라이트"
    ]
  }
}
```

### 현재 화면에서 상품 위치 찾기
**POST** `/api/v1/product/search/location`

현재 카메라 화면에서 특정 상품의 위치를 찾아주는 API

#### Request Headers
| Name | Type | Description | Required |
|------|------|-------------|----------|
| Authorization | String | Bearer {jwt_token} | ✅ |
| Content-Type | String | multipart/form-data | ✅ |

#### Request Body (Form Data)
| Name | Type | Description | Required |
|------|------|-------------|----------|
| current_frame | File | 현재 카메라 화면 (JPEG 형식) | ✅ |
| product_name | String | 찾고자 하는 상품명 | ✅ |

#### Response
```json
{
  "status": 200,
  "message": "상품 방향 안내 성공",
  "result": {
    "case_type": "DIRECTION",
    "direction": "LEFT",
    "confidence": 0.85,
    "detected_products": ["코카콜라", "펩시콜라"]
  }
}
```

### 상품 정보 업데이트 (관리자용)
**POST** `/api/v1/product/seven/drinks`

세븐일레븐 음료 상품 정보를 크롤링하여 업데이트 (관리자용)

#### Query Parameters
| Name | Type | Description | Required | Default |
|------|------|-------------|----------|---------|
| start | Integer | 크롤링 시작 페이지 | ❌ | 1 |
| end | Integer | 크롤링 끝 페이지 | ❌ | 50 |

#### Response
```
HTTP 200 OK (빈 응답)
```

---

## 🗺️ 지도/위치 API

### 근처 편의점 찾기
**GET** `/api/v1/path`

현재 위치 기준 근처 편의점 정보 조회

#### Query Parameters
| Name | Type | Description | Required |
|------|------|-------------|----------|
| lat | Double | 위도 (-90 ~ 90) | ✅ |
| lng | Double | 경도 (-180 ~ 180) | ✅ |

#### Response
```json
{
  "status": 200,
  "message": "근처 편의점 조회 성공",
  "places": [
    {
      "name": "GS25 강남점",
      "address": "서울특별시 강남구 테헤란로 123",
      "distance": 150,
      "lat": 37.4979,
      "lng": 127.0276
    }
  ]
}
```

#### Error Response
```json
{
  "status": 400,
  "message": "잘못된 좌표값입니다.",
  "error": {
    "code": "INVALID_COORDINATE",
    "info": "lat or lng out of valid range"
  }
}
```

---

## 🔍 이미지 분석 API

### Google Vision API 이미지 분석
**POST** `/api/v1/vision/ai/analyze`

Google Cloud Vision API를 이용한 이미지 분석 (사람, 장애물, 카운터, 방향, 선반, 카테고리 인식)

#### Request Headers
| Name | Type | Description | Required |
|------|------|-------------|----------|
| Content-Type | String | multipart/form-data | ✅ |

#### Request Body (Form Data)
| Name | Type | Description | Required |
|------|------|-------------|----------|
| file | File | 분석할 이미지 파일 (최대 10MB) | ✅ |

#### Response
```json
{
  "success": true,
  "message": "이미지 분석이 완료되었습니다.",
  "data": {
    "people": true,
    "obstacles": false,
    "counter": true,
    "direction": "STRAIGHT",
    "shelf": true,
    "category": "beverages"
  },
  "timestamp": 1677123456789
}
```

#### Error Response
```json
{
  "success": false,
  "message": "이미지 분석 중 오류가 발생했습니다.",
  "error": "파일 크기는 10MB를 초과할 수 없습니다.",
  "timestamp": 1677123456789
}
```

---

## 🤖 AI 서비스 API

AI 서비스는 별도 FastAPI 서버(`http://localhost:8083`)에서 제공됩니다.

### Health Check
**GET** `/health`

AI 서비스 상태 확인

#### Response
```json
{
  "status": "healthy",
  "service": "beverage-vision",
  "timestamp": 1677123456.789
}
```

### AI 매대 상품 인식
**POST** `/api/v1/product/search/ai`

YOLO + EfficientNet을 이용한 매대 상품 인식

#### Request Body (Form Data)
| Name | Type | Description | Required |
|------|------|-------------|----------|
| shelf_images | File[] | 매대 이미지들 (다중 이미지 지원) | ✅ |

#### Response
```json
{
  "items": [
    {
      "name": "코카콜라",
      "x": 100,
      "y": 150,
      "w": 80,
      "h": 120
    },
    {
      "name": "스프라이트",
      "x": 200,
      "y": 160,
      "w": 85,
      "h": 115
    }
  ]
}
```

### AI 현재 화면 상품 위치 인식
**POST** `/api/v1/product/search/location/ai`

현재 카메라 화면에서 상품 위치 인식

#### Request Body (Form Data)
| Name | Type | Description | Required |
|------|------|-------------|----------|
| current_frame | File | 현재 카메라 화면 이미지 | ✅ |

#### Response
```json
{
  "multiple": false,
  "items": ["코카콜라"]
}
```

---

## 🔧 관리자 API

### 상품 알레르기 정보 업데이트
**POST** `/api/product-allergy/update`

HACCP 공공데이터를 이용한 상품 알레르기 정보 업데이트

#### Query Parameters
| Name | Type | Description | Required | Default |
|------|------|-------------|----------|---------|
| pageNo | Integer | 페이지 번호 | ❌ | 1 |
| numOfRows | Integer | 페이지당 조회 건수 | ❌ | 100 |

#### Response
```
ProductAllergy 업데이트 완료
```

---

## ⚠️ 공통 에러 코드

| HTTP Status | Error Code | Description |
|-------------|------------|-------------|
| 400 | BAD_REQUEST | 잘못된 요청 파라미터 |
| 401 | UNAUTHORIZED | 인증 토큰이 없거나 유효하지 않음 |
| 403 | FORBIDDEN | 권한 없음 |
| 404 | NOT_FOUND | 리소스를 찾을 수 없음 |
| 500 | INTERNAL_SERVER_ERROR | 서버 내부 오류 |

## 📝 참고사항

- 모든 API 요청 시 `Content-Type: application/json`이 기본이며, 파일 업로드 시에는 `multipart/form-data`를 사용합니다.
- 인증이 필요한 API는 `Authorization: Bearer {jwt_token}` 헤더를 포함해야 합니다.
- 이미지 업로드는 JPEG 형식만 지원하며, 최대 파일 크기는 10MB입니다.
- AI 서비스는 포트 8083에서 별도로 실행되며, CORS가 설정되어 있습니다.
- 타임스탬프는 Unix timestamp (milliseconds) 형식으로 제공됩니다.

## 🚀 서비스 정보

- **Backend API**: `http://localhost:8081` (Production), `http://localhost:8082` (Development)
- **AI Service**: `http://localhost:8083`
- **Swagger UI**: `/swagger-ui.html` (Backend), `/docs` (AI Service)
- **API Documentation**: `/v3/api-docs` (Backend), `/redoc` (AI Service)