# 🌟 SyncMe - Emotion Diary Character Growth Service

> A game-like diary service where your character grows based on your daily mood

## 📋 Project Overview

**SyncMe** is a web service that records users' daily status (energy, burden, passion) and visualizes them through cute characters. 
As you log your daily status, your character's state changes, and you can interact with friends through comments.

### ✨ Key Features

- 🔐 **Google OAuth Login**: Simple social authentication
- 🎮 **Character System**: Characters that change based on your status (Tired/Normal/Energetic)
- 📊 **Status Tracking**: Monitor 3 metrics - Energy, Burden, Passion
- 👥 **Social Features**: Friend follow system and commenting
- ☁️ **Serverless Architecture**: AWS Lambda + DynamoDB

---

## 🛠 Tech Stack

### Backend
- **Framework**: Spring Boot 3.2.5
- **Language**: Java 17
- **Security**: Spring Security + JWT
- **Database**: AWS DynamoDB (NoSQL)
- **Deployment**: AWS Lambda + API Gateway

### Infrastructure
- **Cloud**: AWS (Lambda, DynamoDB, S3, API Gateway)
- **CI/CD**: GitHub Actions
- **Build Tool**: Maven

---

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- AWS CLI (for deployment)

### Local Execution

```bash
# Clone project
git clone <repository-url>
cd syncme

# Run local server
./mvnw spring-boot:run
```

Server URL: `http://localhost:8080`

### Health Check

```bash
# Check server status
curl http://localhost:8080/health
```

---

## 📡 API Endpoints

### 🔓 No Authentication Required

#### Health Check
```http
GET /health
GET /
```

### 🔒 Authentication Required (JWT Token)

> All authenticated APIs require `Authorization: Bearer {token}` in the header

### 1️⃣ Auth - Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/google/login` | Google OAuth Login |
| POST | `/auth/refresh` | Refresh Access Token |

### 2️⃣ User - User Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/users/me` | Get my profile |
| PATCH | `/users/me/nickname` | Update nickname |
| DELETE | `/users/me` | Delete account |
| GET | `/users/search` | Search users |

### 3️⃣ Status - Status Records

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/status/today` | Get today's status |
| POST | `/status/today` | Record today's status |
| PUT | `/status/today` | Update today's status |
| GET | `/status/check` | Check if today's record exists |
| GET | `/status/history` | Get all status history |
| GET | `/status/history/{date}` | Get status by date |
| DELETE | `/status/history` | Reset all records |

### 4️⃣ Character - Character Status

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/character/current` | Get current character state |
| GET | `/character/score` | Get status score (0-100) |
| GET | `/character/summary` | Get character summary |

### 5️⃣ Calculate - Status Calculation

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/calculate/status` | Calculate status result |
| GET | `/calculate/preview` | Preview input values |

### 6️⃣ Friends - Friend System

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/friends/{userId}` | Follow user |
| DELETE | `/friends/{userId}` | Unfollow user |
| GET | `/friends/following` | Get following list |
| GET | `/friends/followers` | Get followers list |
| GET | `/friends` | Get friends list (mutual follow) |
| POST | `/friends/{userId}/comments` | Write comment to friend |
| GET | `/friends/{userId}/comments` | Get friend's received comments |

### 7️⃣ Comments - Comment System

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/comments/received` | Get received comments |
| DELETE | `/comments/{commentId}` | Delete comment |

### 8️⃣ Home - Public Home

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/home/{userId}` | Get user's home |
| GET | `/home/me/share-link` | Get my home share link |

### 9️⃣ Content - UI Content

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/content/today-message` | Get today's message |
| GET | `/content/loading-message` | Get loading message |
| GET | `/content/about` | Get about service |

### 🔟 Settings - Settings

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/settings` | Get all settings |
| DELETE | `/settings/data` | Reset data |

---

## 🔐 Authentication

### Google Login Flow

1. **Login Request**
```json
POST /auth/google/login
{
  "idToken": "Google OAuth ID Token"
}
```

2. **Response (JWT Token Issued)**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": "u_a1b2c3d4e5f6",
    "email": "user@example.com",
    "nickname": "User Name"
  }
}
```

### Token Expiration
- **Access Token**: 24 hours
- **Refresh Token**: 7 days

### Token Refresh

When Access Token expires, refresh with Refresh Token:

```json
POST /auth/refresh
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Authentication Header for API Requests

```http
Authorization: Bearer {access-token}
```

---

## 📦 Project Structure

```
src/
├── main/
│   ├── java/com/syncme/syncme/
│   │   ├── config/              # Configuration (Security, DynamoDB)
│   │   ├── controller/          # REST API Controllers
│   │   ├── dto/                 # Data Transfer Objects
│   │   ├── entity/              # Domain Entities
│   │   ├── repository/          # DynamoDB Repositories
│   │   ├── security/            # JWT & Security
│   │   ├── service/             # Business Logic
│   │   ├── util/                # Utilities
│   │   └── exception/           # Exception Handlers
│   └── resources/
│       └── application.properties
└── test/                        # Unit & Integration Tests
```

---

## 🌐 Deployment (AWS Lambda)

### Build & Deploy

```bash
# 1. Build (skip tests)
./mvnw clean package -DskipTests

# 2. Upload to S3
aws s3 cp target/syncme-0.0.1-SNAPSHOT-aws.jar \
  s3://syncme-lambda-deploy/ \
  --region ap-northeast-2

# 3. Update Lambda function
aws lambda update-function-code \
  --function-name syncme-backend \
  --s3-bucket syncme-lambda-deploy \
  --s3-key syncme-0.0.1-SNAPSHOT-aws.jar \
  --region ap-northeast-2 \
  --no-cli-pager
```

### Production URL

```
https://lrcc5bl2sj.execute-api.ap-northeast-2.amazonaws.com/default
```

---

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run specific test
./mvnw test -Dtest=ServiceTests
```

---

## 📚 Documentation

- [API_ENDPOINTS_bySusie.md](API_ENDPOINTS_bySusie.md) - Complete API Specification
- [Cookbook_bySusie.md](Cookbook_bySusie.md) - Development Guide & Planning
- [DEPLOYMENT.md](DEPLOYMENT.md) - Deployment Guide

--

**Made with 💙 by ECC Team 1**
