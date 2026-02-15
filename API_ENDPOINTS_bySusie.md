# SyncMe API 엔드포인트 목록

## 서버 실행
```bash
./mvnw spring-boot:run
```
- 서버 주소: http://localhost:8080
- Lambda 배포: https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default

---

## 0. Health Check (인증 불필요)

### 0.1 헬스체크
```http
GET /health
```

**Request:** 없음

**Response:**
```json
{
  "status": "UP",
  "message": "SyncMe Backend is running"
}
```

### 0.2 루트 엔드포인트
```http
GET /
```

**Request:** 없음

**Response:**
```json
{
  "service": "SyncMe Backend API",
  "version": "1.0.0"
}
```

---

## 1. Auth API (인증 관련)

### 1.1 Google 로그인
```http
POST /auth/google/login
Content-Type: application/json
```

**설명:** Google OAuth 2.0 ID Token을 검증하고 JWT 토큰을 발급합니다.

**Request:**
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE2N..."
}
```

> **Note:** 
> - `idToken`은 Google에서 발급한 실제 ID Token (매우 긴 문자열)
> - Google OAuth Playground (https://developers.google.com/oauthplayground/) 에서 테스트용 토큰 발급 가능
> - ID Token 유효기간: 약 1시간
> - 프론트엔드에서는 Google Sign-In 라이브러리를 사용하여 자동 발급

**Response (성공):**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjk2MTIzNDU2LCJleHAiOjE2OTYyMDk4NTZ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwidHlwZSI6InJlZnJlc2giLCJpYXQiOjE2OTYxMjM0NTYsImV4cCI6MTY5NjcyODI1Nn0...",
    "userId": "u_a1b2c3d4e5f6",
    "email": "user@example.com",
    "nickname": "User Name"
  }
}
```

> **Token 유효기간:**
> - Access Token (token): 24시간
> - Refresh Token (refreshToken): 7일


**Response (실패 - 잘못된 토큰):**
```json
{
  "success": false,
  "message": "Invalid Google token",
  "data": null
}
```

**Response (실패 - 이메일 미검증):**
```json
{
  "success": false,
  "message": "Email not verified by Google",
  "data": null
}
```

### 1.2 토큰 갱신 (Refresh Token)
```http
POST /auth/refresh
Content-Type: application/json
```

**설명:** Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다.

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwidHlwZSI6InJlZnJlc2giLCJpYXQiOjE2OTYxMjM0NTYsImV4cCI6MTY5NjcyODI1Nn0..."
}
```

**Response (성공):**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjk2MjA5ODU2LCJleHAiOjE2OTYyOTYyNTZ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwidHlwZSI6InJlZnJlc2giLCJpYXQiOjE2OTYyMDk4NTYsImV4cCI6MTY5NjgxNDY1Nn0...",
    "userId": "u_a1b2c3d4e5f6",
    "email": "user@example.com",
    "nickname": "User Name"
  }
}
```

**Response (실패 - 잘못된 Refresh Token):**
```json
{
  "success": false,
  "message": "Invalid refresh token",
  "data": null
}
```

**Response (실패 - 만료된 Refresh Token):**
```json
{
  "success": false,
  "message": "Refresh token expired",
  "data": null
}
```

> **사용 시나리오:**
> 1. 로그인 시 `token`과 `refreshToken` 모두 저장
> 2. API 요청 시 Access Token (`token`) 사용
> 3. Access Token 만료 시 (401 Unauthorized) Refresh Token으로 갱신
> 4. 새로운 Access Token과 Refresh Token으로 업데이트
> 5. Refresh Token도 만료된 경우 재로그인 필요

### 1.3 로그아웃
```http
POST /auth/logout
```

**Request:** 없음

**Response:**
```json
{
  "success": true,
  "message": "Logged out successfully",
  "data": null
}
```

---

## 2. User API (사용자 정보)

### 2.1 내 계정 정보 조회
```http
GET /users/me
Authorization: Bearer {토큰}
```

**Request:** 없음 (Authorization 헤더 필요)

**Response:**
```json
{
  "success": true,
  "message": null,
  "data": {
    "userId": "u_a1b2c3d4e5f6",
    "email": "test@example.com",
    "nickname": "User_test"
  }
}
```

### 2.2 닉네임 변경
```http
PATCH /users/me/nickname
Authorization: Bearer {토큰}
Content-Type: application/json
```

**Request:**
```json
{
  "nickname": "새로운닉네임"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Nickname updated successfully",
  "data": {
    "userId": "u_a1b2c3d4e5f6",
    "email": "test@example.com",
    "nickname": "새로운닉네임"
  }
}
```

### 2.3 사용자 검색
```http
GET /users/search?query={검색어}&type={검색타입}
Authorization: Bearer {토큰}
```

**설명:** 닉네임 또는 이메일로 사용자를 검색합니다.

**Query Parameters:**
- `query`: 검색어 (required)
- `type`: 검색 타입 - `nickname` 또는 `email` (optional, 기본값: `nickname`)

**Request:** 없음 (Authorization 헤더 및 query parameters 필요)

**Response:**
```json
{
  "success": true,
  "message": null,
  "data": [
    {
      "userId": "u_a1b2c3d4e5f6",
      "email": "user@example.com",
      "nickname": "찾는사용자"
    },
    {
      "userId": "u_g7h8i9j0k1l2",
      "email": "another@example.com",
      "nickname": "찾는닉네임2"
    }
  ]
}
```

### 2.4 계정 삭제
```http
DELETE /users/me
Authorization: Bearer {토큰}
```

**Request:** 없음 (Authorization 헤더 필요)

**Response:**
```json
{
  "success": true,
  "message": "User deleted successfully",
  "data": null
}
```

---

## 3. Content API (UI 콘텐츠) - 인증 불필요

### 3.1 오늘의 한 마디
```http
GET /content/today-message
```

**Request:** 없음

**Response:**
```json
{
  "success": true,
  "message": null,
  "data": {
    "message": "오늘도 화이팅!"
  }
}
```

### 3.2 로딩 메시지
```http
GET /content/loading-message
```

**Request:** 없음

**Response:**
```json
{
  "success": true,
  "message": null,
  "data": {
    "message": "잠시만 기다려주세요..."
  }
}
```

### 3.3 앱 소개
```http
GET /content/about
```

**Request:** 없음

**Response:**
```json
{
  "success": true,
  "message": null,
  "data": {
    "message": "SyncMe는 일상의 상태를 기록하고 캐릭터와 함께 성장하는 서비스입니다. 매일의 에너지, 부담감, 열정을 기록하면 캐릭터가 변화하며 당신의 상태를 시각적으로 보여줍니다. Version 1.0.0"
  }
}
```

---

## 4. Friends API (친구 기능)

### 4.1 친구 팔로우
```http
POST /friends/{userId}
Authorization: Bearer {토큰}
```

**설명:** 다른 사용자를 팔로우합니다.

**Request:** 없음 (Authorization 헤더 및 URL의 userId 필요)

**Path Parameter:**
- `userId`: 팔로우할 사용자의 userId (예: `u_a1b2c3d4e5f6`)

**Response (성공):**
```json
{
  "success": true,
  "message": "Followed successfully",
  "data": {
    "userId": "u_a1b2c3d4e5f6",
    "email": "friend@example.com",
    "nickname": "Friend User",
    "followedAt": "2026-01-26T12:00:00"
  }
}
```

**Response (실패 - 자기 자신 팔로우):**
```json
{
  "success": false,
  "message": "Cannot follow yourself",
  "data": null
}
```

**Response (실패 - 이미 팔로우 중):**
```json
{
  "success": false,
  "message": "Already following this user",
  "data": null
}
```

### 4.2 친구 언팔로우
```http
DELETE /friends/{userId}
Authorization: Bearer {토큰}
```

**설명:** 팔로우 중인 사용자를 언팔로우합니다.

**Request:** 없음 (Authorization 헤더 및 URL의 userId 필요)

**Path Parameter:**
- `userId`: 언팔로우할 사용자의 userId

**Response:**
```json
{
  "success": true,
  "message": "Unfollowed successfully",
  "data": null
}
```

### 4.3 팔로잉 목록 조회
```http
GET /friends/following
Authorization: Bearer {토큰}
```

**설명:** 내가 팔로우하고 있는 사용자 목록을 조회합니다.

**Request:** 없음 (Authorization 헤더 필요)

**Response:**
```json
{
  "success": true,
  "message": null,
  "data": [
    {
      "userId": "u_a1b2c3d4e5f6",
      "email": "friend1@example.com",
      "nickname": "Friend 1",
      "followedAt": "2026-01-25T10:30:00"
    },
    {
      "userId": "u_g7h8i9j0k1l2",
      "email": "friend2@example.com",
      "nickname": "Friend 2",
      "followedAt": "2026-01-26T09:15:00"
    }
  ]
}
```

### 4.4 팔로워 목록 조회
```http
GET /friends/followers
Authorization: Bearer {토큰}
```

**설명:** 나를 팔로우하고 있는 사용자 목록을 조회합니다.

**Request:** 없음 (Authorization 헤더 필요)

**Response:**
```json
{
  "success": true,
  "message": null,
  "data": [
    {
      "userId": "u_m3n4o5p6q7r8",
      "email": "follower1@example.com",
      "nickname": "Follower 1",
      "followedAt": "2026-01-24T14:20:00"
    },
    {
      "userId": "u_s9t0u1v2w3x4",
      "email": "follower2@example.com",
      "nickname": "Follower 2",
      "followedAt": "2026-01-26T11:45:00"
    }
  ]
}
```

### 4.5 친구 목록 조회 (상호 팔로우)
```http
GET /friends
Authorization: Bearer {토큰}
```

**설명:** 서로 팔로우하고 있는 친구 목록을 조회합니다.

**Request:** 없음 (Authorization 헤더 필요)

**Response:**
```json
{
  "success": true,
  "message": null,
  "data": [
    {
      "userId": "u_a1b2c3d4e5f6",
      "email": "friend@example.com",
      "nickname": "Mutual Friend",
      "followedAt": "2026-01-25T10:30:00"
    }
  ]
}
```

---

## 5. Home API (공개 마이홈)

### 5.1 공개 홈 조회
```http
GET /home/{userId}
Authorization: Bearer {토큰} (선택사항)
```

**설명:** 사용자의 공개 홈을 조회합니다. 인증 없이도 접근 가능하며, 인증된 상태에서는 팔로우 여부가 포함됩니다.

**Path Parameter:**
- `userId`: 조회할 사용자의 userId

**Request:** 없음 (Authorization 헤더 선택사항)

**Response (인증 없이 조회):**
```json
{
  "success": true,
  "message": null,
  "data": {
    "userId": "u_a1b2c3d4e5f6",
    "nickname": "Public User",
    "isFollowing": false
  }
}
```

**Response (인증된 상태로 조회):**
```json
{
  "success": true,
  "message": null,
  "data": {
    "userId": "u_a1b2c3d4e5f6",
    "nickname": "Public User",
    "isFollowing": true
  }
}
```

**Response (실패 - 사용자 없음):**
```json
{
  "success": false,
  "message": "User not found",
  "data": null
}
```

### 5.2 공유 링크 생성
```http
GET /home/me/share-link
Authorization: Bearer {토큰}
```

**설명:** 내 홈 페이지의 공유 링크를 생성합니다. 다른 사용자에게 공유할 수 있습니다.

**Request:** 없음 (Authorization 헤더 필요)

**Response:**
```json
{
  "success": true,
  "message": null,
  "data": {
    "shareLink": "https://syncme-frontend.vercel.app/home/u_a1b2c3d4e5f6",
    "userId": "u_a1b2c3d4e5f6"
  }
}
```

---

## 6. Settings API (설정)

### 6.1 설정 정보 조회
```http
GET /settings
Authorization: Bearer {토큰}
```

**Request:** 없음 (Authorization 헤더 필요)

**Response:**
```json
{
  "success": true,
  "message": null,
  "data": {    
    "userId": "u_a1b2c3d4e5f6",    
    "email": "test@example.com",
    "nickname": "User_test",
    "version": "1.0.0"
  }
}
```

### 6.2 데이터 삭제
```http
DELETE /settings/data
Authorization: Bearer {토큰}
```

**설명:** 사용자의 모든 데이터(Status History 등)를 삭제합니다. 사용자 계정 자체는 유지됩니다.

**Request:** 없음 (Authorization 헤더 필요)

**Response:**
```json
{
  "success": true,
  "message": "Data deleted successfully",
  "data": null
}
```

> **Note:** 
> - 이 엔드포인트는 Status History 데이터를 삭제합니다
> - 사용자 계정(User) 정보는 유지됩니다 (닉네임, 이메일 등)
> - 계정 완전 삭제는 `DELETE /users/me` 사용

---

## cURL 테스트 예제

### 0. 헬스체크
```bash
# 로컬
curl http://localhost:8080/health

# Lambda
curl https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default/health
```

### 1. Google OAuth Playground에서 ID Token 발급

1. https://developers.google.com/oauthplayground/ 접속
2. 설정(⚙️)에서 본인의 OAuth Client ID/Secret 입력 (선택사항)
3. Scope 선택:
   - `https://www.googleapis.com/auth/userinfo.email`
   - `https://www.googleapis.com/auth/userinfo.profile`
   - `openid`
4. "Authorize APIs" 클릭 후 Google 로그인
5. "Exchange authorization code for tokens" 클릭
6. `id_token` 값 복사

### 2. 로그인하여 JWT 토큰 받기
```bash
# ID Token을 변수에 저장 (Google OAuth Playground에서 받은 값)
ID_TOKEN="eyJhbGciOiJSUzI1NiIsImtpZCI6IjE2N..."

# 로컬
curl -X POST http://localhost:8080/auth/google/login \
  -H "Content-Type: application/json" \
  -d "{\"idToken\": \"$ID_TOKEN\"}"

# Lambda
curl -X POST https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default/auth/google/login \
  -H "Content-Type: application/json" \
  -d "{\"idToken\": \"$ID_TOKEN\"}"

# 또는 jq로 보기 좋게 출력
curl -X POST http://localhost:8080/auth/google/login \
  -H "Content-Type: application/json" \
  -d "{\"idToken\": \"$ID_TOKEN\"}" | jq '.'
```

### 3. JWT 토큰을 사용하여 사용자 정보 조회
```bash
# 위에서 받은 JWT 토큰을 여기에 입력
TOKEN="여기에_받은_JWT_토큰_입력"

# 로컬
curl -X GET http://localhost:8080/users/me \
  -H "Authorization: Bearer $TOKEN"

# Lambda
curl -X GET https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default/users/me \
  -H "Authorization: Bearer $TOKEN"
```

### 4. 닉네임 변경
```bash
# 로컬
curl -X PATCH http://localhost:8080/users/me/nickname \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nickname": "새닉네임"}'

# Lambda
curl -X PATCH https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default/users/me/nickname \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nickname": "새닉네임"}'
```

### 5. 오늘의 메시지 (인증 불필요)
```bash
# 로컬
curl http://localhost:8080/content/today-message

# Lambda
curl https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default/content/today-message
```

### 6. 설정 조회
```bash
# 로컬
curl http://localhost:8080/settings \
  -H "Authorization: Bearer $TOKEN"

# Lambda
curl https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default/settings \
  -H "Authorization: Bearer $TOKEN"
```

### 7. 계정 삭제
```bash
# 로컬
curl -X DELETE http://localhost:8080/users/me \
  -H "Authorization: Bearer $TOKEN"

# Lambda
curl -X DELETE https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default/users/me \
  -H "Authorization: Bearer $TOKEN"
```

---

## Postman 테스트 순서

### 사전 준비: Google ID Token 발급

1. **Google OAuth Playground 접속**
   - URL: https://developers.google.com/oauthplayground/

2. **설정 (선택사항)**
   - 오른쪽 상단 ⚙️ 클릭
   - "Use your own OAuth credentials" 체크
   - OAuth Client ID와 Client secret 입력 (본인의 Google Cloud 프로젝트)

3. **Scope 선택**
   - Step 1에서 다음 scope 선택:
     - ✅ `https://www.googleapis.com/auth/userinfo.email`
     - ✅ `https://www.googleapis.com/auth/userinfo.profile`
     - ✅ `openid`
   - "Authorize APIs" 버튼 클릭

4. **Google 로그인**
   - Google 계정 선택 및 권한 승인

5. **Token 발급**
   - Step 2에서 "Exchange authorization code for tokens" 버튼 클릭
   - `id_token` 필드의 값을 복사 (매우 긴 문자열)

### Postman 테스트

1. **Google 로그인** (`POST /auth/google/login`)
   - URL: `http://localhost:8080/auth/google/login` 또는 Lambda URL
   - Method: POST
   - Headers: `Content-Type: application/json`
   - Body (raw JSON):
     ```json
     {
       "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE2N2RjM..."
     }
     ```
     > ⚠️ `idToken`에 위에서 복사한 Google ID Token을 붙여넣기
   - Send 클릭
   - 응답에서 `data.token` 값을 복사 (이것이 JWT 토큰)

2. **Authorization 설정**
   - Type: Bearer Token
   - Token: 복사한 토큰 값 입력

3. **다른 엔드포인트 테스트**
   - 인증이 필요한 API는 모두 위 토큰 사용
   - Headers에 `Authorization: Bearer {token}` 추가

---

## API 응답 구조

모든 API는 다음 형식의 `ApiResponse` 래퍼를 사용합니다:

```json
{
  "success": true,       // 성공 여부 (boolean)
  "message": "...",      // 메시지 (String, optional)
  "data": { ... }        // 실제 데이터 (Object, optional)
}
```

### 성공 응답 예시 - Google OAuth 2.0 ID Token"
}
```
> Google에서 발급한 ID Token을 검증합니다. 프론트엔드에서는 Google Sign-In 라이브러리를 사용하여 자동으로 발급받습니다.
  "success": true,
  "message": "Nickname updated successfully",
  "data": {
    "email": "test@example.com",
    "nickname": "새닉네임"
  }
}
```

### 에러 응답 예시
```json
{
  "success": false,
  "message": "User not found",
  "data": null
}
```

---

## DTO 구조

### GoogleLoginRequest
```json
{
  "idToken": "string (required, not blank)"
}
```

### AuthResponse
```json
{
  "token": "string",
  "refreshToken": "string",
  "userId": "string",
  "email": "string",
  "nickname": "string"
}
```

### UserResponse
```json
{
  "userId": "string",
  "email": "string",
  "nickname": "string"
}
```

### FriendResponse
```json
{
  "userId": "string",
  "email": "string",
  "nickname": "string",
  "followedAt": "string (ISO 8601 datetime)"
}
```

### HomeResponse
```json
{
  "userId": "string",
  "nickname": "string",
  "isFollowing": "boolean"
}
```

### UserSearchResponse
```json
{
  "userId": "string",
  "email": "string",
  "nickname": "string"
}
```

### ShareLinkResponse
```json
{
  "shareLink": "string (URL)",
  "userId": "string"
}
```Google ID Token**: 
  - Google에서 발급한 실제 ID Token 필요
  - 유효기간: 약 1시간
  - 테스트: Google OAuth Playground 사용
  - 프로덕션: 프론트엔드에서 Google Sign-In 라이브러리 사용
- **JWT 토큰 유효기간**: 
  - Access Token: 24시간 (86400000ms)
  - Refresh Token: 14일 (1209600000ms)
- **인증 불필요 엔드포인트**: 
  - `/health`
  - `/`
  - `/auth/google/login`
  - `/auth/refresh`
  - `/auth/logout`
  - `/content/**`
  - `/home/**` (공개 접근, 인증 선택)
- **인증 필요 엔드포인트**: 
  - `/auth/me`
  - `/users/**`
  - `/friends/**`
  - `/settings/**`
- **데이터베이스**: 
  - DynamoDB Tables:
    - `syncme-users` (with `userId-index`, `googleId-index` GSI)
    - `syncme-daily-status`
    - `syncme-friends` (with `targetUserId-index` GSI)
- **CORS**: 모든 origin 허용 (프로덕션에서는 특정 도메인만 허용 권장)

## 환경 변수

다음 환경 변수가 설정되어 있어야 합니다:

- **로컬 개발**:
  ```bash
  export GOOGLE_CLIENT_ID="your-client-id.apps.googleusercontent.com"
  export GOOGLE_CLIENT_SECRET="your-client-secret"
  export JWT_SECRET="your-jwt-secret-key"
  ```

- **AWS Lambda**:
  - Lambda 콘솔 → Configuration → Environment variables에서 설정
  - `GOOGLE_CLIENT_ID`
  - `GOOGLE_CLIENT_SECRET`
  - `JWT_SECRET`
  - `SPRING_PROFILES_ACTIVE=prod`

### SettingsResponse
```json
{
  "userId": "string",
  "email": "string",
  "nickname": "string",
  "version": "string"
}
```

---

## 주의사항

- **JWT 토큰 유효기간**: 
  - Access Token: 24시간 (86400000ms)
  - Refresh Token: 14일 (1209600000ms)
- **인증 불필요 엔드포인트**: 
  - `/health`
  - `/`
  - `/auth/google/login`
  - `/auth/refresh`
  - `/auth/logout`
  - `/content/**`
  - `/home/**` (공개 접근, 인증 선택)
- **인증 필요 엔드포인트**: 
  - `/auth/me`
  - `/users/**`
  - `/friends/**`
  - `/settings/**`
- **데이터베이스**: 
  - DynamoDB Tables:
    - `syncme-users` (with `userId-index`, `googleId-index` GSI)
    - `syncme-daily-status`
    - `syncme-friends` (with `targetUserId-index` GSI)
- **CORS**: 모든 origin 허용 (프로덕션에서는 특정 도메인만 허용 권장)

---

## 🧪 9. 프론트엔드 테스트용 계정 정보

> **💡 사용 방법**: Postman에서 API 한 번 호출 → 응답에서 토큰 복사 → 프론트엔드에 전달

---

### 📝 테스트 계정 생성 방법

**1. Postman에서 API 호출:**

```
POST https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default/admin/test-data?days=14
```

**2. 응답에서 토큰 복사:**

응답 예시:
```json
{
  "success": true,
  "data": {
    "mainAccount": {
      "userId": "u_abc123def456",
      "email": "testmain@syncme.com",
      "nickname": "TestMain User",
      "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0bWFpbkBzeW5jbWUuY29tIiwiaWF0IjoxNjk2MTIzNDU2LCJleHAiOjE2OTYyMDk4NTZ9...",
      "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0bWFpbkBzeW5jbWUuY29tIiwidHlwZSI6InJlZnJlc2giLCJpYXQiOjE2OTYxMjM0NTYsImV4cCI6MTY5NjcyODI1Nn0..."
    },
    "friendAccounts": [
      {
        "userId": "u_111222333444",
        "email": "testfriend1@syncme.com",
        "nickname": "TestFriend 1",
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
      },
      {
        "userId": "u_222333444555",
        "email": "testfriend2@syncme.com",
        "nickname": "TestFriend 2",
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
      },
      {
        "userId": "u_333444555666",
        "email": "testfriend3@syncme.com",
        "nickname": "TestFriend 3",
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
      }
    ],
    "daysOfData": 14
  }
}
```

**3. 프론트엔드에 전달할 정보:**

슬랙/메시지로 다음 정보만 전달:

```
📱 메인 테스트 계정
Email: testmain@syncme.com
Access Token: eyJhbGciOiJIUzI1NiJ9... (전체 복사)
Refresh Token: eyJhbGciOiJIUzI1NiJ9... (전체 복사)

👥 친구1 계정
Email: testfriend1@syncme.com
Access Token: eyJhbGciOiJIUzI1NiJ9...
Refresh Token: eyJhbGciOiJIUzI1NiJ9...

👥 친구2 계정
Email: testfriend2@syncme.com
Access Token: eyJhbGciOiJIUzI1NiJ9...
Refresh Token: eyJhbGciOiJIUzI1NiJ9...

👥 친구3 계정
Email: testfriend3@syncme.com
Access Token: eyJhbGciOiJIUzI1NiJ9...
Refresh Token: eyJhbGciOiJIUzI1NiJ9...
```

---

### 🔌 API 상세 정보

**Endpoint:**
```http
POST /admin/test-data?days=14
```

**Full URL:**
```
https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default/admin/test-data?days=14
```

**Query Parameters:**
- `days` (optional): 생성할 과거 데이터 일수 (기본값: 14, 범위: 1~90)

**생성되는 내용:**
- 4개의 User 계정 (DynamoDB에 실제 저장)
- 각 계정별 과거 14일간의 기분 데이터 (Energy, Burden, Passion)
- 메인 계정 → 친구 3명 팔로우 관계 설정
- 각 계정의 유효한 JWT 토큰 (Access Token 24시간, Refresh Token 7일)

**생성되는 데이터:**
- 4개의 User 레코드 (DynamoDB `syncme-users` 테이블)
- 각 계정별 N일 × 4 = 총 N×4개의 DailyStatus 레코드
- 3개의 Friend 관계 (메인 → 친구1, 친구2, 친구3)
- 각 계정의 유효한 JWT Access Token 및 Refresh Token

**직접 API 호출 (필요시):**
```bash
# Lambda 배포 서버로 호출
curl -X POST "https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default/admin/test-data?days=14"
```

---

### 🗑️ 테스트 데이터 삭제 API

```http
---

### 🗑️ 테스트 데이터 삭제 (필요시)

**Postman에서 호출:**
```
DELETE https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default/admin/test-data
```

**응답:**
```json
{
  "success": true,
  "message": "Test data deleted successfully",
  "data": null
}
```

---

## 📊 테스트 계정 정보

| 계정 | Email | Nickname | 역할 |
|------|-------|----------|------|
| Main | `testmain@syncme.com` | TestMain User | 메인 테스트 계정 (친구 3명 팔로우) |
| Friend 1 | `testfriend1@syncme.com` | TestFriend 1 | 친구 계정 1 |
| Friend 2 | `testfriend2@syncme.com` | TestFriend 2 | 친구 계정 2 |
| Friend 3 | `testfriend3@syncme.com` | TestFriend 3 | 친구 계정 3 |

### 프론트엔드 사용 예시

```javascript
// 백엔드에서 받은 토큰
const accessToken = "eyJhbGciOiJIUzI1NiJ9...";  // Postman 응답에서 복사
const refreshToken = "eyJhbGciOiJIUzI1NiJ9...";

// localStorage에 저장
localStorage.setItem('accessToken', accessToken);
localStorage.setItem('refreshToken', refreshToken);

// API 호출
const API_URL = 'https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default';

fetch(`${API_URL}/users/me`, {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
});
```

### 생성되는 데이터

- **기분 데이터**: 각 계정별 과거 14일 (Energy, Burden, Passion)
- **데이터 범위**: 20~90 (자연스러운 패턴)
- **Friend 관계**: 메인 계정이 친구 3명 모두 팔로우
- **토큰 유효기간**: Access Token 24시간, Refresh Token 7일

---

## ⚠️ 주의사항

- 토큰 만료 시 Postman에서 API 다시 호출하여 새 토큰 생성
- 프로덕션 배포 전 `/admin/**` 엔드포인트 및 관련 코드 제거 필요