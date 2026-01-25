# 카카오 소셜 로그인 API 명세서

## 기본 정보
- **Base URL**: `http://localhost:8080/api`
- **Content-Type**: `application/json`

---

## 1. 카카오 로그인 콜백 (POST)

### Endpoint
```
POST /auth/kakao/callback
```

### 설명
카카오 인가 코드를 받아 JWT 토큰을 발급합니다.

### Request Body
```json
{
  "code": "카카오_인가_코드"
}
```

### Response (200 OK)
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "user@kakao.com",
    "nickname": "홍길동",
    "profileImage": "https://k.kakaocdn.net/..."
  }
}
```

### Error Response
```json
{
  "code": "KAKAO_AUTH_FAILED",
  "message": "카카오 인증에 실패했습니다"
}
```

---

## 2. 카카오 로그인 콜백 (GET)

### Endpoint
```
GET /auth/kakao/callback?code={인가코드}
```

### 설명
카카오 Redirect로 받은 인가 코드를 처리합니다. (프론트엔드 Redirect용)

### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| code | String | Y | 카카오 인가 코드 |

### Response
POST 방식과 동일

---

## 3. 토큰 갱신

### Endpoint
```
POST /auth/refresh
```

### 설명
Refresh Token으로 새로운 Access Token과 Refresh Token을 발급받습니다.

### Request Headers
```
Authorization: Bearer {refreshToken}
```

### Response (200 OK)
```json
{
  "accessToken": "새로운_액세스_토큰",
  "refreshToken": "새로운_리프레시_토큰"
}
```

### Error Response
```json
{
  "code": "INVALID_TOKEN",
  "message": "유효하지 않은 토큰입니다"
}
```

---

## 4. Mock 로그인 (개발용)

### Endpoint
```
POST /auth/mock/login
```

### 설명
개발 환경에서 카카오 인증 없이 바로 JWT 토큰을 발급받습니다.

### Request Body
```json
{
  "email": "test@example.com",
  "nickname": "테스트유저"
}
```

### Response (200 OK)
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "test@example.com",
    "nickname": "테스트유저",
    "profileImage": "https://via.placeholder.com/150"
  }
}
```

---

## 카카오 로그인 플로우

### 프론트엔드에서 구현할 흐름

```javascript
// 1. 카카오 로그인 버튼 클릭 시
const KAKAO_AUTH_URL = `https://kauth.kakao.com/oauth/authorize?client_id={YOUR_KAKAO_CLIENT_ID}&redirect_uri=http://localhost:3000/auth/kakao/callback&response_type=code`;

window.location.href = KAKAO_AUTH_URL;

// 2. 카카오 로그인 후 Redirect URI로 돌아올 때 (예: /auth/kakao/callback)
const urlParams = new URLSearchParams(window.location.search);
const code = urlParams.get('code');

// 3. 백엔드에 인가 코드 전송
const response = await fetch('http://localhost:8080/api/auth/kakao/callback', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({ code }),
});

const data = await response.json();
// data.accessToken, data.refreshToken, data.user 저장

// 4. 이후 API 요청 시 Authorization 헤더에 포함
fetch('http://localhost:8080/api/users/me', {
  headers: {
    'Authorization': `Bearer ${data.accessToken}`,
  },
});
```

---

## 토큰 관리 권장 사항

### Access Token
- **저장 위치**: 메모리 (변수)
- **만료 시간**: 1시간
- **용도**: API 요청 시 사용

### Refresh Token
- **저장 위치**: HttpOnly Cookie (권장) 또는 LocalStorage
- **만료 시간**: 7일
- **용도**: Access Token 만료 시 갱신

### 토큰 갱신 시나리오
```javascript
// API 요청 실패 시 (401 Unauthorized)
if (error.status === 401) {
  // Refresh Token으로 토큰 갱신
  const refreshResponse = await fetch('http://localhost:8080/api/auth/refresh', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${refreshToken}`,
    },
  });
  
  const { accessToken, refreshToken: newRefreshToken } = await refreshResponse.json();
  
  // 새 토큰 저장 후 재요청
  // ...
}
```

---

## 환경 변수 설정

### 백엔드 (application.yml)
```yaml
kakao:
  oauth:
    client-id: YOUR_KAKAO_REST_API_KEY
    client-secret: YOUR_KAKAO_CLIENT_SECRET
```

### 프론트엔드 (.env)
```
REACT_APP_KAKAO_CLIENT_ID=YOUR_KAKAO_JAVASCRIPT_KEY
REACT_APP_REDIRECT_URI=http://localhost:3000/auth/kakao/callback
REACT_APP_API_BASE_URL=http://localhost:8080/api
```

---

## 에러 코드

| 코드 | HTTP Status | 설명 |
|------|-------------|------|
| INVALID_TOKEN | 401 | 유효하지 않은 토큰 |
| EXPIRED_TOKEN | 401 | 만료된 토큰 |
| KAKAO_AUTH_FAILED | 400 | 카카오 인증 실패 |
| USER_NOT_FOUND | 404 | 사용자를 찾을 수 없음 |
| DUPLICATE_EMAIL | 409 | 이미 사용 중인 이메일 |

---

## 테스트 방법

### Postman으로 테스트하기

#### 1. Mock 로그인으로 빠른 테스트
```bash
POST http://localhost:8080/api/auth/mock/login
Content-Type: application/json

{
  "email": "test@example.com",
  "nickname": "테스트유저"
}
```

#### 2. 실제 카카오 로그인 테스트
1. 브라우저에서 카카오 로그인 URL 접속
2. 로그인 후 Redirect URI에서 `code` 파라미터 복사
3. Postman에서 `/auth/kakao/callback` 호출

---

## 주의사항

1. **HTTPS 필수**: 프로덕션 환경에서는 반드시 HTTPS 사용
2. **JWT Secret 변경**: `application.yml`의 `jwt.secret` 값을 충분히 긴 랜덤 문자열로 변경
3. **Redirect URI 일치**: 카카오 개발자 콘솔에 등록한 Redirect URI와 정확히 일치해야 함
4. **CORS 설정**: 프론트엔드 도메인에서 API 호출 시 CORS 설정 필요
5. **Token 보안**: Access Token은 노출되지 않도록 주의 (HTTPS 사용)
