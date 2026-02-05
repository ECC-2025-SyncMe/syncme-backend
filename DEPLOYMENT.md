# AWS Lambda 배포 가이드

이 문서는 SyncMe 프로젝트를 AWS Lambda, API Gateway, DynamoDB를 사용하여 서버리스로 배포하는 전체 과정을 설명합니다.

---

## 📋 목차
1. [사전 준비사항](#1-사전-준비사항)
2. [프로젝트 구조 설명](#2-프로젝트-구조-설명)
3. [DynamoDB 테이블 생성](#3-dynamodb-테이블-생성)
4. [Lambda 함수 배포 패키지 생성](#4-lambda-함수-배포-패키지-생성)
5. [Lambda 함수 생성 및 배포](#5-lambda-함수-생성-및-배포)
6. [API Gateway 설정](#6-api-gateway-설정)
7. [테스트](#7-테스트)
8. [문제 해결](#8-문제-해결)
9. [GitHub Actions CI/CD 설정](#9-github-actions-cicd-설정)

---

## 1. 사전 준비사항

### 필요한 것
- AWS 계정
- AWS CLI 설치 및 구성
- Maven 설치
- Java 17 이상

### AWS CLI 설치 및 구성
```bash
# AWS CLI 설치 확인
aws --version

# AWS 자격 증명 설정
aws configure
# AWS Access Key ID: [입력]
# AWS Secret Access Key: [입력]
# Default region name: ap-northeast-2
# Default output format: json
```

---

## 2. 프로젝트 구조 설명

### 2.1 주요 파일 및 역할

#### Lambda Handler
```java
StreamLambdaHandler.java
```
- AWS Lambda의 진입점
- API Gateway 요청을 Spring Boot 애플리케이션으로 프록시
- `SpringBootLambdaContainerHandler`를 사용하여 요청 처리

#### 핵심 설정 파일
- `pom.xml`: Maven 의존성 및 빌드 설정
  - `aws-serverless-java-container-springboot3` 의존성
  - `maven-shade-plugin`으로 AWS 배포용 JAR 생성
- `SecurityConfig.java`: Spring Security 설정
  - `/health`, `/api/auth/**` 등 공개 엔드포인트 설정
  - JWT 인증 필터 추가
- `DynamoDBConfig.java`: DynamoDB 클라이언트 설정

#### 구현된 기능 (담당 부분)
1. **Auth API** (`AuthController.java`)
   - Google 로그인
   - 로그아웃
   - 현재 사용자 확인

2. **User API** (`UserController.java`)
   - 계정 정보 조회
   - 닉네임 변경
   - 계정 삭제

3. **Content API** (`ContentController.java`)
   - 오늘의 한 마디 조회
   - About 정보 조회

4. **Settings API** (`SettingsController.java`)
   - 설정 조회
   - 데이터 초기화

### 2.2 주요 의존성
```xml
<!-- Spring Boot 3.2.5 -->
<spring-boot.version>3.2.5</spring-boot.version>

<!-- AWS Serverless Java Container -->
<dependency>
    <groupId>com.amazonaws.serverless</groupId>
    <artifactId>aws-serverless-java-container-springboot3</artifactId>
    <version>2.0.3</version>
</dependency>

<!-- AWS SDK for DynamoDB Enhanced -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>dynamodb-enhanced</artifactId>
</dependency>
```

---

## 3. DynamoDB 테이블 생성

### 3.1 AWS 콘솔에서 테이블 생성

1. **AWS Management Console** 접속
2. **DynamoDB** 서비스로 이동
3. **테이블 만들기** 클릭

### 3.2 테이블 설정

```
테이블 이름: syncme-users

파티션 키: email (String)

테이블 설정: 
- 온디맨드 모드 선택 (자동 확장)
```

4. **테이블 만들기** 클릭

### 3.3 GSI (Global Secondary Index) 추가

1. 생성된 `syncme-users` 테이블 클릭
2. **인덱스** 탭 선택
3. **인덱스 생성** 클릭

```
인덱스 이름: googleId-index
파티션 키: googleId (String)
```

4. **인덱스 생성** 클릭

### 3.4 CLI로 테이블 생성 (선택사항)

```bash
# 테이블 생성
aws dynamodb create-table \
    --table-name syncme-users \
    --attribute-definitions \
        AttributeName=email,AttributeType=S \
        AttributeName=googleId,AttributeType=S \
    --key-schema \
        AttributeName=email,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --region ap-northeast-2

# GSI 추가
aws dynamodb update-table \
    --table-name syncme-users \
    --attribute-definitions AttributeName=googleId,AttributeType=S \
    --global-secondary-index-updates \
    "[{\"Create\":{\"IndexName\":\"googleId-index\",\"KeySchema\":[{\"AttributeName\":\"googleId\",\"KeyType\":\"HASH\"}],\"Projection\":{\"ProjectionType\":\"ALL\"}}}]" \
    --region ap-northeast-2
```

---

## 4. Lambda 함수 배포 패키지 생성

### 4.1 프로젝트 빌드

```bash
# 프로젝트 루트 디렉토리로 이동
cd /path/to/syncme

# Maven 빌드
./mvnw clean package

# 빌드 성공 확인
ls -lh target/syncme-0.0.1-SNAPSHOT-aws.jar
```

### 3.2 JAR 파일 확인

빌드가 완료되면 다음 파일이 생성됩니다:
```
target/syncme-0.0.1-SNAPSHOT-aws.jar
```

이 파일이 Lambda에 배포할 패키지입니다. (약 50-100MB)

---

## 4. Lambda 함수 생성 및 배포

### 4.1 Lambda 함수 생성

1. **AWS Lambda 콘솔** 접속
2. **함수 만들기** 클릭
3. **새로 작성** 선택

```
함수 이름: syncme-backend
런타임: Java 17
아키텍처: x86_64
```

4. **권한** 섹션에서 **기본 실행 역할 변경** 선택
   - **새 역할을 AWS 정책 템플릿에서 생성** 선택
   - 역할 이름: `syncme-backend-role`
   - 정책 템플릿: `단순 마이크로서비스 권한` 선택

5. **함수 만들기** 클릭

### 4.2 IAM 역할에 DynamoDB 권한 추가

함수가 생성되면 자동으로 생성된 역할에 DynamoDB 권한을 추가해야 합니다:

1. Lambda 함수 페이지에서 **구성** 탭 → **권한** 선택
2. **역할 이름** 클릭 (IAM 콘솔로 이동)
3. **권한 추가** → **인라인 정책 생성** 클릭
4. JSON 탭 선택하고 다음 정책 입력:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "dynamodb:GetItem",
        "dynamodb:PutItem",
        "dynamodb:UpdateItem",
        "dynamodb:DeleteItem",
        "dynamodb:Query",
        "dynamodb:Scan"
      ],
      "Resource": [
        "arn:aws:dynamodb:ap-northeast-2:*:table/syncme-users",
        "arn:aws:dynamodb:ap-northeast-2:*:table/syncme-users/index/*"
      ]
    }
  ]
}
```

5. **정책 검토** 클릭
6. 정책 이름: `syncme-dynamodb-policy` 입력
7. **정책 생성** 클릭

### 4.3 함수 코드 업로드

#### 방법 1: AWS 콘솔에서 업로드

1. Lambda 함수 페이지에서 **코드** 탭 선택
2. **업로드** → **파일** 선택
3. `target/syncme-0.0.1-SNAPSHOT-aws.jar` 파일 선택
4. **저장** 클릭

#### 방법 2: AWS CLI 사용

```bash
aws lambda update-function-code \
    --function-name syncme-backend \
    --zip-file fileb://target/syncme-0.0.1-SNAPSHOT-aws.jar \
    --region ap-northeast-2
```

### 4.4 Lambda 함수 구성 설정

#### ⚙️ 일반 구성
1. **구성** 탭 → **일반 구성** → **편집** 클릭

```
메모리: 1024 MB (최소 512MB 권장)
제한 시간: 30초 (API 응답 시간 고려)
```

2. **저장** 클릭

#### 🔐 환경 변수 설정
1. **구성** 탭 → **환경 변수** → **편집** 클릭
2. **환경 변수 추가** 클릭

```
키: JWT_SECRET
값: your-super-secret-jwt-key-minimum-256-bits-required

키: AWS_REGION
값: ap-northeast-2

키: SPRING_PROFILES_ACTIVE
값: prod
```

3. **저장** 클릭

⚠️ **보안 주의사항**: 
- JWT_SECRET은 최소 256비트(32자 이상) 필요
- 프로덕션에서는 AWS Secrets Manager 사용 권장

### 4.5 핸들러 설정

1. **코드** 탭 → **런타임 설정** → **편집** 클릭

```
핸들러: com.syncme.syncme.StreamLambdaHandler::handleRequest
```

2. **저장** 클릭

💡 **중요**: `StreamLambdaHandler`는 API Gateway 프록시 통합을 위한 핸들러입니다.

---

## 5. API Gateway 설정

### 5.1 API Gateway 생성

1. **API Gateway 콘솔** 접속
2. **API 생성** 클릭
3. **HTTP API** 선택 (REST API도 가능하지만 HTTP API가 더 간단)
4. **구축** 클릭

```
API 이름: syncme-backend-api
```

### 5.2 Lambda 통합 추가

1. **통합 추가** 클릭
2. **Lambda** 선택

```
Lambda 함수: syncme-backend
버전: $LATEST
```

3. **다음** 클릭

### 5.3 라우트 구성

#### 프록시 통합 설정 (권장)

```
메서드: ANY
리소스 경로: /{proxy+}
통합 대상: syncme-backend
```

이렇게 설정하면 모든 HTTP 메서드와 경로가 Lambda로 전달됩니다.

#### 개별 라우트 설정 (선택사항)

담당 부분의 엔드포인트만 설정:

```
POST   /auth/google/login
POST   /auth/logout
GET    /auth/me

GET    /users/me
PATCH  /users/me/nickname
DELETE /users/me

GET    /content/today-message
GET    /content/loading-message
GET    /content/about

GET    /settings
POST   /settings/reset
```

### 5.4 스테이지 생성

1. **스테이지** → **스테이지 생성** 클릭

```
스테이지 이름: default
자동 배포: 활성화
```

2. **생성** 클릭

### 5.5 API 엔드포인트 URL 확인

스테이지 페이지에서 **호출 URL** 확인:
```
예: https://abc123xyz.execute-api.ap-northeast-2.amazonaws.com/default
```

이 URL이 프론트엔드에서 사용할 API 기본 URL입니다.

### 5.6 CORS 설정 (프론트엔드 연동 시 필요)

1. **CORS** 메뉴 선택
2. **구성** 클릭

```
Access-Control-Allow-Origin: * (또는 프론트엔드 도메인)
Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
Access-Control-Allow-Headers: Content-Type, Authorization
Access-Control-Max-Age: 300
```

3. **저장** 클릭

---

## 7. 테스트

### 7.1 배포 후 빠른 테스트

#### S3와 Lambda 업데이트 (한 줄로)
```bash
./mvnw clean package -DskipTests && \
aws s3 cp target/syncme-0.0.1-SNAPSHOT-aws.jar s3://syncme-lambda-deploy/ --region ap-northeast-2 && \
aws lambda update-function-code \
  --function-name syncme-backend \
  --s3-bucket syncme-lambda-deploy \
  --s3-key syncme-0.0.1-SNAPSHOT-aws.jar \
  --region ap-northeast-2 \
  --no-cli-pager
```

#### API Gateway URL로 테스트
```bash
# API Gateway URL 설정 (본인의 URL로 변경)
API_URL="https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default"

# 1. 헬스체크 (인증 불필요)
curl $API_URL/health

# 2. 루트 엔드포인트
curl $API_URL/

# 3. About 정보 조회 (인증 불필요)
curl $API_URL/content/about

# 4. 오늘의 메시지 (인증 불필요)
curl $API_URL/content/today-message

# 5. 로딩 메시지 (인증 불필요)
curl $API_URL/content/loading-message
```

### 7.2 인증 관련 API 테스트

#### 구글 로그인 (테스트)
```bash
# 로그인 - 임시 idToken 사용
curl -X POST $API_URL/auth/google \
  -H "Content-Type: application/json" \
  -d '{
    "idToken": "test@example.com"
  }'

# 응답 예시:
# {
#   "token": "eyJhbGciOiJIUzI1NiJ9...",
#   "email": "test@example.com",
#   "nickname": "User_test"
# }
```

#### 로그아웃
```bash
curl -X POST $API_URL/auth/logout
```

#### 현재 사용자 확인 (JWT 필요)
```bash
# 위에서 받은 JWT 토큰 사용
JWT_TOKEN="eyJhbGciOiJIUzI1NiJ9..."

curl $API_URL/auth/me \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 7.3 사용자 관리 API 테스트

#### 내 정보 조회
```bash
curl $API_URL/users/me \
  -H "Authorization: Bearer $JWT_TOKEN"
```

#### 닉네임 변경
```bash
curl -X PATCH $API_URL/users/me/nickname \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nickname": "새로운닉네임"
  }'
```

#### 계정 삭제
```bash
curl -X DELETE $API_URL/users/me \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 7.4 설정 관련 API 테스트

#### 설정 조회
```bash
curl $API_URL/settings \
  -H "Authorization: Bearer $JWT_TOKEN"
```

#### 데이터 초기화
```bash
curl -X POST $API_URL/settings/reset \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 8.2 API Gateway에서 Internal Server Error

**증상**: `{"message":"Internal Server Error"}` 응답

**원인**:
1. Lambda 함수 핸들러가 잘못 설정됨
2. Lambda에 DynamoDB 권한이 없음
3. 환경 변수가 설정되지 않음

**해결**:
```bash
# 1. 핸들러 확인
핸들러: com.syncme.syncme.StreamLambdaHandler::handleRequest

# 2. IAM 역할 DynamoDB 권한 확인
# 3. 환경 변수 확인
aws lambda get-function-configuration \
  --function-name syncme-backend \
  --region ap-northeast-2 \
  --query 'Environment'

# 4. CloudWatch 로그 확인
aws logs tail /aws/lambda/syncme-backend --follow --region ap-northeast-2
```

### 8.3 CORS 에러

**증상**: 브라우저 콘솔에 CORS 에러

**해결**:
1. API Gateway CORS 설정 확인
2. SecurityConfig.java의 CORS 설정 확인
3. 다시 배포 후 API Gateway 재배포

### 8.4 JWT 토큰 검증 실패

**증상**: `401 Unauthorized` 또는 JWT 관련 에러

**해결**:
```bash
# Lambda 환경 변수 확인
aws lambda get-function-configuration \
  --function-name syncme-backend \
  --region ap-northeast-2

# JWT_SECRET이 256비트 이상인지 확인
# 환경 변수 업데이트
aws lambda update-function-configuration \
  --function-name syncme-backend \
  --environment Variables="{JWT_SECRET=your-secret-key-here,AWS_REGION=ap-northeast-2}" \
  --region ap-northeast-2
```

### 8.5 DynamoDB 접근 에러

**증상**: `AccessDeniedException` 또는 DynamoDB 관련 에러

**해결**:
1. IAM 역할에 DynamoDB 권한 추가
2. 테이블 이름이 `syncme-users`인지 확인
3. Region이 `ap-northeast-2`인지 확인

### 8.6 Lombok 관련 IDE 경고

**증상**: IDE에서 `@Builder`, `@Getter` 등을 인식하지 못함

**상태**: Maven 빌드는 정상 작동 (실제 컴파일 문제 없음)

**원인**: VS Code/NetBeans Java Language Server와 Lombok 프로세서 충돌

**해결**:
- IDE 경고는 무시해도 됨
- `mvn compile` 명령어로 실제 컴파일 확인
- 실제 배포 및 실행에는 영향 없음

---

## 9. GitHub Actions CI/CD 설정

### 9.1 IAM 사용자 생성

1. **IAM 콘솔** → **사용자** → **사용자 생성** 클릭

```
사용자 이름: syncme-cicd-user
```

2. **권한 설정** → **직접 정책 연결**

#### S3 및 Lambda 배포 권한 정책

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "lambda:UpdateFunctionCode",
        "lambda:GetFunction",
        "lambda:UpdateFunctionConfiguration"
      ],
      "Resource": "arn:aws:lambda:ap-northeast-2:*:function:syncme-backend"
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::syncme-lambda-deploy",
        "arn:aws:s3:::syncme-lambda-deploy/*"
      ]
    }
  ]
}
```

3. **사용자 생성** 클릭

### 9.3 Access Key 발급

1. 생성된 사용자 클릭
2. **보안 자격 증명** 탭
3. **액세스 키 만들기** 클릭
4. **명령줄 인터페이스(CLI)** 선택
5. **액세스 키 만들기** 클릭
6. **액세스 키 ID**와 **비밀 액세스 키** 복사 및 저장

⚠️ **중요**: 비밀 액세스 키는 이 화면에서만 확인 가능합니다!

### 9.4 GitHub Secrets 설정

1. GitHub 저장소 → **Settings** → **Secrets and variables** → **Actions**
2. **New repository secret** 클릭
3. 다음 시크릿 추가:

```
Name: AWS_ACCESS_KEY_ID
Secret: [Access Key ID]

Name: AWS_SECRET_ACCESS_KEY
Secret: [Secret Access Key]

Name: AWS_REGION
Secret: ap-northeast-2

Name: JWT_SECRET
Secret: [JWT Secret Key]
```

### 9.5 GitHub Actions 워크플로우 작성

프로젝트 루트에 `.github/workflows/deploy.yml` 파일 생성:

```yaml
name: Deploy to AWS Lambda

on:
  push:
    branches:
      - main
      - develop

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      
      - name: Build with Maven
        run: ./mvnw clean package -DskipTests
      
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v2
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ secrets.AWS_REGION }}
      
      - name: Upload to S3
        run: |
          aws s3 cp target/syncme-0.0.1-SNAPSHOT-aws.jar \
            s3://syncme-lambda-deploy/ \
            --region ${{ secrets.AWS_REGION }}
      
      - name: Deploy to Lambda from S3
        run: |
          aws lambda update-function-code \
            --function-name syncme-backend \
            --s3-bucket syncme-lambda-deploy \
            --s3-key syncme-0.0.1-SNAPSHOT-aws.jar \
            --region ${{ secrets.AWS_REGION }}
      
      - name: Update Lambda environment variables
        run: |
          aws lambda update-function-configuration \
            --function-name syncme-backend \
            --environment Variables="{JWT_SECRET=${{ secrets.JWT_SECRET }},AWS_REGION=${{ secrets.AWS_REGION }},SPRING_PROFILES_ACTIVE=prod}" \
            --region ${{ secrets.AWS_REGION }}
      
      - name: Wait for Lambda update
        run: |
          aws lambda wait function-updated \
            --function-name syncme-backend \
            --region ${{ secrets.AWS_REGION }}
      
      - name: Verify deployment
        run: |
          aws lambda get-function \
            --function-name syncme-backend \
            --region ${{ secrets.AWS_REGION }} \
            --query 'Configuration.[FunctionName,LastModified,State]' \
            --output text
```

### 9.6 CI/CD 테스트

1. 워크플로우 파일을 커밋하고 푸시:

```bash
git add .github/workflows/deploy.yml
git 9.7 배포 확인

```bash
# API 엔드포인트 테스트
curl -X GET "https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default/health
3. 모든 단계가 성공했는지 확인

### 7.6 배포 확인

```bash
# API 엔드포인트 테스트
curl -X GET "https://your-api-gateway-url/prod/content/about"
```

---

## 📊 배포 아키텍처

```
[Client/Frontend]
    ↓ HTTPS
[API Gateway] - https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default
    ↓ Lambda Proxy Integration
[Lambda Function: syncme-backend]
  - Handler: StreamLambdaHandler
  - Runtime: Java 17
  - Memory: 1024 MB
  - Timeout: 30s
    ↓
[Spring Boot Application]
  - Controllers: Auth, User, Content, Settings
  - Services & Repositories
  - JWT Authentication
    ↓
[DynamoDB: syncme-users]
  - Partition Key: email
  - GSI: googleId-index
    ↓
[CloudWatch Logs: /aws/lambda/syncme-backend]
```

---

## 🔧 문제 해결 (트러블슈팅)

### 1. Lambda 함수 타임아웃

**증상**: Lambda 함수가 30초 내에 응답하지 않음

**해결**:
- Lambda 제한 시간을 늘림 (최대 15분)
- Cold start 문제 → Provisioned Concurrency 설정
- 메모리 증가 (CPU 성능 향상)

### 2. DynamoDB 권한 에러

**증상**: `AccessDeniedException` 발생

**해결**:
- IAM 역할의 DynamoDB 권한 확인
- 테이블 ARN이 정확한지 확인
- GSI 권한도 포함되어 있는지 확인

### 3. JWT 검증 실패

**증상**: `SignatureException` 발생

**해결**:
- Lambda 환경 변수의 `JWT_SECRET` 확인
- 시크릿 키가 256비트 이상인지 확인
- 환경 변수가 올바르게 설정되었는지 확인

### 4. CORS 에러

**증상**: 브라우저에서 `CORS policy` 에러

**해결**:
- API Gateway CORS 설정 확인
- `Access-Control-Allow-Origin` 헤더 확인
- OPTIONS 메서드 허용 확인

### 5. Cold Start 지연

**증상**: 첫 요청이 매우 느림 (5-10초)

**해결**:
- Provisioned Concurrency 설정 (비용 증가)
- 메모리 증가 (1024MB 이상)
- Warm-up 람다 함수 추가

---

## 📈 확장 가능성

### 1. Multi-Region 배포
- 여러 리전에 Lambda 배포
- Route 53으로 트래픽 분산

### 2. DynamoDB 글로벌 테이블
- 여러 리전에서 데이터 동기화
- 낮은 지연 시간 제공

### 3. API Gateway 캐싱
- 자주 조회되는 데이터 캐싱
- 응답 시간 개선

### 4. CloudFront CDN
- API Gateway 앞단에 배치
- 정적 콘텐츠 캐싱

### 5. AWS X-Ray
- 분산 추적 활성화
- 성능 병목 지점 파악

---

## 💰 비용 추정

### 예상 사용량 (월간)
- API 요청: 100,000건
- Lambda 실행: 100,000회 (평균 1초)
- DynamoDB 읽기/쓰기: 200,000건

### 예상 비용
```
Lambda: $0.20 (무료 티어 포함)
API Gateway: $0.10
DynamoDB: $0.25 (온디맨드)
CloudWatch Logs: $0.05

총 예상 비용: 약 $0.60/월
```

---

## 📝 테스트 체크리스트

### 구현 완료된 API (내가 담당한 부분)

#### 1. Auth API ✅
- [ ] `POST /api/auth/google` - Google 로그인
- [ ] `POST /api/auth/logout` - 로그아웃
- [ ] `GET /api/auth/me` - 현재 사용자 확인

#### 2. User API ✅
- [ ] `GET /api/users/me` - 내 계정 정보 조회
- [ ] `PATCH /api/users/me/nickname` - 닉네임 변경
- [ ] `DELETE /api/users/me` - 계정 삭제

#### 3. Content API ✅
- [ ] `GET /api/content/today-message` - 오늘의 한 마디
- [ ] `GET /api/content/loading-message` - 로딩 메시지
- [ ] `GET /api/content/about` - 앱 정보

#### 4. Settings API ✅
- [ ] `GET /api/settings` - 설정 조회
- [ ] `POST /api/settings/reset` - 데이터 초기화

#### 5. Health Check ✅
- [ ] `GET /health` - 헬스체크
- [ ] `GET /` - 루트 엔드포인트

### 테스트 순서

#### 1단계: 로컬 테스트 (선택사항)
```bash
# Spring Boot 로컬 실행
./mvnw spring-boot:run

# 각 API 엔드포인트를 localhost:8080으로 테스트
curl http://localhost:8080/health
curl http://localhost:8080/api/content/about
```

#### 2단계: Unit Test 실행
```bash
./mvnw test
```

#### 3단계: Lambda 빌드 및 배포
```bash
# Maven 빌드
./mvnw clean package -DskipTests

# S3 업로드
aws s3 cp target/syncme-0.0.1-SNAPSHOT-aws.jar s3://syncme-lambda-deploy/ --region ap-northeast-2

# Lambda 업데이트
aws lambda update-function-code \
  --function-name syncme-backend \
  --s3-bucket syncme-lambda-deploy \
  --s3-key syncme-0.0.1-SNAPSHOT-aws.jar \
  --region ap-northeast-2 \
  --no-cli-pager

# Lambda 업데이트 대기 (중요!)
aws lambda wait function-updated --function-name syncme-backend --region ap-northeast-2
```

#### 4단계: API Gateway 테스트
```bash
# API Gateway URL 설정
API_URL="https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default"

# 1. 헬스체크 (인증 불필요)
curl -X GET "$API_URL/health"
# 예상 응답: {"status":"UP","timestamp":"2025-06-01T12:00:00.000+00:00"}

curl -X GET "$API_URL/"
# 예상 응답: {"serviceName":"SyncMe Backend","version":"1.0.0","status":"running"}

# 2. Content API (인증 불필요)
curl -X GET "$API_URL/api/content/about"
# 예상 응답: {"title":"싱크미","description":"...","version":"1.0.0"}

curl -X GET "$API_URL/api/content/today-message"
# 예상 응답: {"message":"오늘도 좋은 하루 되세요!"}

curl -X GET "$API_URL/api/content/loading-message"
# 예상 응답: {"message":"잠시만 기다려주세요..."}

# 3. Auth API - 로그인 (인증 불필요)
curl -X POST "$API_URL/api/auth/google" \
  -H "Content-Type: application/json" \
  -d '{"idToken":"test@example.com"}'
# 예상 응답: {"userId":"...","email":"test@example.com","token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}

# 4. 받은 JWT 토큰 저장 (위에서 받은 token 값)
JWT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# 5. User API - 내 정보 조회 (인증 필요)
curl -X GET "$API_URL/api/users/me" \
  -H "Authorization: Bearer $JWT_TOKEN"
# 예상 응답: {"email":"test@example.com","nickname":"사용자123","createdAt":"2025-06-01T..."}

# 6. User API - 닉네임 변경 (인증 필요)
curl -X PATCH "$API_URL/api/users/me/nickname" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nickname":"새로운닉네임"}'
# 예상 응답: {"email":"test@example.com","nickname":"새로운닉네임","createdAt":"...","updatedAt":"..."}

# 7. Settings API - 설정 조회 (인증 필요)
curl -X GET "$API_URL/api/settings" \
  -H "Authorization: Bearer $JWT_TOKEN"
# 예상 응답: {"notificationsEnabled":true,"theme":"light"}

# 8. Settings API - 데이터 초기화 (인증 필요)
curl -X POST "$API_URL/api/settings/reset" \
  -H "Authorization: Bearer $JWT_TOKEN"
# 예상 응답: {"message":"모든 데이터가 초기화되었습니다."}

# 9. Auth API - 현재 사용자 확인 (인증 필요)
curl -X GET "$API_URL/api/auth/me" \
  -H "Authorization: Bearer $JWT_TOKEN"
# 예상 응답: {"email":"test@example.com","nickname":"새로운닉네임"}

# 10. Auth API - 로그아웃 (인증 필요)
curl -X POST "$API_URL/api/auth/logout" \
  -H "Authorization: Bearer $JWT_TOKEN"
# 예상 응답: {"message":"로그아웃되었습니다."}

# 11. User API - 계정 삭제 (인증 필요, 마지막 테스트)
curl -X DELETE "$API_URL/api/users/me" \
  -H "Authorization: Bearer $JWT_TOKEN"
# 예상 응답: {"message":"계정이 삭제되었습니다."}
```

#### 5단계: DynamoDB 데이터 확인
```bash
# 사용자 데이터 확인
aws dynamodb scan --table-name syncme-users --region ap-northeast-2

# 특정 사용자 조회
aws dynamodb get-item \
  --table-name syncme-users \
  --key '{"email":{"S":"test@example.com"}}' \
  --region ap-northeast-2
```

#### 6단계: CloudWatch 로그 확인
```bash
# 최근 로그 실시간 모니터링
aws logs tail /aws/lambda/syncme-backend --follow --region ap-northeast-2

# 에러 로그만 필터링
aws logs tail /aws/lambda/syncme-backend --follow --filter-pattern "ERROR" --region ap-northeast-2
```

#### 7단계: GitHub Actions CI/CD 확인
1. 코드 변경 후 main 브랜치에 push
2. GitHub 저장소 → **Actions** 탭
3. 워크플로우 실행 확인
4. 모든 단계 성공 확인
5. 자동 배포 완료 후 API 테스트 재실행

---

## 📋 전체 배포 체크리스트

배포 전 확인사항:

### AWS 인프라 설정
- [ ] DynamoDB 테이블 생성 완료 (`syncme-users`)
- [ ] DynamoDB Partition Key 설정 완료 (`email: String`)
- [ ] GSI 생성 완료 (`googleId-index`, Partition Key: `googleId`)
- [ ] S3 버킷 생성 완료 (`syncme-lambda-deploy`)
- [ ] S3 버킷 버전 관리 활성화 (선택사항)

### Lambda 설정
- [ ] Lambda 함수 생성 완료 (`syncme-backend`)
- [ ] IAM 역할 DynamoDB 권한 설정 완료 (`AmazonDynamoDBFullAccess`)
- [ ] IAM 역할 CloudWatch Logs 권한 확인
- [ ] Lambda 핸들러 설정 완료 (`com.syncme.syncme.StreamLambdaHandler::handleRequest`)
- [ ] Lambda 런타임 설정 완료 (Java 17)
- [ ] Lambda 메모리 설정 완료 (1024 MB)
- [ ] Lambda 타임아웃 설정 완료 (30초)
- [ ] Lambda 환경 변수 설정 완료:
  - [ ] `JWT_SECRET`: JWT 시크릿 키
  - [ ] `AWS_REGION`: `ap-northeast-2`

### API Gateway 설정
- [ ] API Gateway (HTTP API) 생성 완료
- [ ] Lambda 통합 생성 완료
- [ ] 프록시 통합 활성화 (`ANY /{proxy+}`)
- [ ] CORS 설정 완료 (모든 origin 허용)
- [ ] API Gateway 배포 완료 (스테이지: `default`)
- [ ] API Gateway URL 확인 (`https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default`)

### 코드 배포
- [ ] Maven 빌드 성공 (`./mvnw clean package -DskipTests`)
- [ ] JAR 파일 S3 업로드 완료
- [ ] Lambda 코드 업데이트 완료 (S3 버킷 연동)
- [ ] Lambda 함수 업데이트 완료 대기

### 테스트 확인
- [ ] 헬스체크 테스트 성공 (`GET /health`, `GET /`)
- [ ] Content API 테스트 성공 (인증 불필요)
  - [ ] `GET /api/content/about`
  - [ ] `GET /api/content/today-message`
  - [ ] `GET /api/content/loading-message`
- [ ] Auth API - 로그인 테스트 성공 (`POST /api/auth/google`)
- [ ] JWT 토큰 발급 확인
- [ ] User API 테스트 성공 (인증 필요)
  - [ ] `GET /api/users/me`
  - [ ] `PATCH /api/users/me/nickname`
- [ ] Settings API 테스트 성공 (인증 필요)
  - [ ] `GET /api/settings`
  - [ ] `POST /api/settings/reset`
- [ ] Auth API 테스트 성공 (인증 필요)
  - [ ] `GET /api/auth/me`
  - [ ] `POST /api/auth/logout`
- [ ] User API - 계정 삭제 테스트 성공 (`DELETE /api/users/me`)

### 데이터 및 로그 확인
- [ ] DynamoDB 데이터 확인 완료 (사용자 생성/업데이트/삭제)
- [ ] CloudWatch 로그 정상 출력 확인
- [ ] 에러 로그 없음 확인

### CI/CD 설정
- [ ] GitHub Secrets 설정 완료:
  - [ ] `AWS_ACCESS_KEY_ID`
  - [ ] `AWS_SECRET_ACCESS_KEY`
  - [ ] `AWS_REGION`
  - [ ] `JWT_SECRET`
- [ ] `.github/workflows/deploy.yml` 파일 생성 완료
- [ ] GitHub Actions 워크플로우 테스트 성공
- [ ] 자동 배포 성공 확인
- [ ] 배포 후 API 테스트 재확인

---

## 🎉 배포 완료!

모든 체크리스트를 완료하면 서버리스 API가 정상적으로 배포됩니다.

**API 엔드포인트:**
```
https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default
```

**구현 완료된 기능:**
- ✅ Google OAuth 로그인 (JWT 토큰 발급)
- ✅ 사용자 정보 조회/수정/삭제
- ✅ 컨텐츠 API (앱 정보, 오늘의 메시지 등)
- ✅ 설정 관리 (조회/초기화)
- ✅ 헬스체크 및 모니터링

**미구현 기능 (팀원 담당):**
- ⏳ Status API (상태 관리)
- ⏳ Character API (캐릭터 관리)
- ⏳ Calculation API (계산 기능)

이 URL을 프론트엔드에서 사용하세요!
