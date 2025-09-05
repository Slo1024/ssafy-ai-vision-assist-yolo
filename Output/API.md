# 회원 API
![image](/uploads/e1dcffecc686853caabc26c5dfcd2dfe/image.png){width=854 height=260}
## 회원가입 및 로그인
### request header
| type | value |
| --- | --- |
| Content-Type | application/json; charset=utf8 |
| Type  | BEARER |
| Access-Token | userNo |

### response header
| type | value |
| --- | --- |
| Content-Type | application/json; charset=utf8 |

## 알레르기 정보 입력
### request header
| type | value |
| --- | --- |
| Content-Type | application/json; charset=utf8 |
| Type  | BEARER |
| Access-Token | userNo |

### request parameter
| 파라미터명 | 타입 | 설명 | 필수 | 길이 |
| --- | --- | --- | --- | --- |
| request.allergy_id | INT | 알레르기 id | O |  |

### response parameter
| 파라미터명 | 자료형 타입 | 설명 |
| --- | --- | --- |
| status | STRING | 반환코드 |
| message | STRING | 메시지 |
| result | STRING | null |

## 알레르기 정보 삭제
### request header
| 이름 | 설명 | 예시 |
| --- | --- | --- |
| Content-Type | 요청 데이터 타입 | application/json; charset=utf8 |
| Authorization | JWT 인증 토큰 (Bearer 방식 사용) | Bearer {jwtToken} |

### request parameter
| 파라미터명 | 타입 | 설명 | 필수 | 길이 |
| --- | --- | --- | --- | --- |
| request.allergy_id | INT | 알레르기 id | O |  |

```json
{
  "allergy_id" : 1
}
```

### response header
| type | value |
| --- | --- |
| Content-Type | application/json; charset=utf8 |

### response parameter
| 파라미터명 | 자료형 타입 | 설명 |
| --- | --- | --- |
| status | Integer | HTTP 상태 코드 |
| message | STRING | 응답 메시지 |
| result | null | 반환 없음 |

## 알레르기 목록
### request header
| 이름 | 설명 | 예시 |
| --- | --- | --- |
| Content-Type | 요청 데이터 타입 | application/json; charset=utf8 |
| Authorization | JWT 인증 토큰 (Bearer 방식 사용) | Bearer {jwtToken} |


### response header
| type | value |
| --- | --- |
| Content-Type | application/json; charset=utf8 |

### response parameter
| 파라미터명 | 자료형 타입 | 설명 |
| --- | --- | --- |
| status | Integer | HTTP 상태 코드 |
| message | STRING | 응답 메시지 |
| result | OBJECT | 응답 데이터 |
| result.names[] | ARRAY | 알러지 명 리스트 |
| result.names[].allergy_id | INT | 알러지 id |
| result.names[].name | STRING | 알러지 명 |

## 알레르기 검색
### request header
| 이름 | 설명 | 예시 |
| --- | --- | --- |
| Content-Type | 요청 데이터 타입 | application/json; charset=utf8 |
| Authorization | JWT 인증 토큰 (Bearer 방식 사용) | Bearer {jwtToken} |

### response header
| type | value |
| --- | --- |
| Content-Type | application/json; charset=utf8 |

### response parameter
| 파라미터명 | 자료형 타입 | 설명 |
| --- | --- | --- |
| status | Integer | HTTP 상태 코드 |
| message | STRING | 응답 메시지 |
| result | STRING | 응답 데이터 |

### success data example
```json
{
	"status" : 200,
	"message" : "해당 알레르기가 검색되었습니다.",
	"result" : [
		{
		"allegy_name": "소고기",
		"allegy_id":"2"
		},
				{
		"allegy_name": "돼지고기",
		"allegy_id":"3"
		},
	]
}
```

---

# 상품 API
![image](/uploads/64db2c58f4fd80af2a8510208ead7585/image.png){width=861 height=229}
## 장바구니 목록
### request header
| 이름 | 설명 | 예시 |
| --- | --- | --- |
| Content-Type | 요청 데이터 타입 | application/json; charset=utf8 |
| Authorization | JWT 인증 토큰 (Bearer 방식 사용) | Bearer {jwtToken} |

### response header
| type | value |
| --- | --- |
| Content-Type | application/json; charset=utf8 |

### response parameter
| 파라미터명 | 자료형 타입 | 설명 |
| --- | --- | --- |
| status | Integer | HTTP 상태 코드 |
| message | STRING | 응답 메시지 |
| result | OBJECT | 응답 데이터 |
| result.items | Array<Object> | 장바구니 목록 |
| result.items[].cart_id | Integer | 카트 ID |
| result.items[].product_id | Integer | 상품 ID |
| result.items[].product_name | String | 상품 이름 |

### success data example
```json
{
	"status" : 201,
	"message" : "장바구니 목록 조회 성공",
	"result" : {
		"items": [
          {
		         "cart_id" : 1,
              "product_id": 1,
              "product_name": "코카콜라"
          },
          {
		          "cart_id" : 2,
              "product_id": 5,
              "product_name": "포카칩"
          }
      ]
	}
}
```

## 상품 검색
### request header
| 이름 | 설명 | 예시 |
| --- | --- | --- |
| Content-Type | 요청 데이터 타입 | application/json; charset=utf8 |
| Authorization | JWT 인증 토큰 (Bearer 방식 사용) | Bearer {jwtToken} |

### response header
| type | value |
| --- | --- |
| Content-Type | application/json; charset=utf8 |

### response parameter
| 파라미터명 | 자료형 타입 | 설명 |
| --- | --- | --- |
| status | Integer | HTTP 상태 코드 |
| message | STRING | 응답 메시지 |
| result | OBJECT | 응답 데이터 |
| result.items | Array<Object> | 상품 목록 |
| result.items[].product_id | Integer | 상품 ID |
| result.items[].product_name | String | 상품 이름 |

### success data example
```json
{
	"status" : 201,
	"message" : "검색 성공",
	"result" : {
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

## 장바구니 담기
### request header
| 이름 | 설명 | 예시 |
| --- | --- | --- |
| Content-Type | 요청 데이터 타입 | application/json; charset=utf8 |
| Authorization | JWT 인증 토큰 (Bearer 방식 사용) | Bearer {jwtToken} |

### request parameter
| 파라미터명 | 타입 | 설명 | 필수 | 길이 |
| --- | --- | --- | --- | --- |
| request.product_id | INT | 상품 id | O |  |

### response header
| type | value |
| --- | --- |
| Content-Type | application/json; charset=utf8 |

### response parameter
| 파라미터명 | 자료형 타입 | 설명 |
| --- | --- | --- |
| status | Integer | HTTP 상태 코드 |
| message | STRING | 응답 메시지 |
| result | OBJECT | 응답 데이터 |

### success data example
```json
{
	"status" : 201,
	"message" : "장바구니에 상품을 담았습니다.",
	"result" : null
}
```
## 장바구니 삭제
### request header
| 이름 | 설명 | 예시 |
| --- | --- | --- |
| Content-Type | 요청 데이터 타입 | application/json; charset=utf8 |
| Authorization | JWT 인증 토큰 (Bearer 방식 사용) | Bearer {jwtToken} |

### request parameter
| 파라미터명 | 타입 | 설명 | 필수 | 길이 |
| --- | --- | --- | --- | --- |
| request.cart_id | INT | 카트 id | O |  |

### response header
| type | value |
| --- | --- |
| Content-Type | application/json; charset=utf8 |

### response parameter
| 파라미터명 | 자료형 타입 | 설명 |
| --- | --- | --- |
| status | Integer | HTTP 상태 코드 |
| message | STRING | 응답 메시지 |
| result | OBJECT | 응답 데이터 |

### success data example
```json
{
	"status" : 201,
	"message" : "장바구니에서 삭제하였습니다.",
	"result" : null
}
```
---

# 지도 API
![image](/uploads/a5aed84cac7176a04228418e14adee64/image.png){width=776 height=149}

---

# AI API
![image](/uploads/1391c3072cf106c9ca1a8fdf6d7e343f/image.png){width=884 height=311}