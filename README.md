# FitPick Backend

FitPick은 오프라인 스마트 의류 매장을 위한 **Android 기반 O2O 쇼핑 서비스**의 백엔드입니다.
사용자는 매장 내 옷에 부착된 **NFC 태그를 인식해 상품 정보·옵션을 즉시 확인**하고, 장바구니/바로 주문을 통해 모의 결제를 진행한 뒤 매장에서 픽업할 수 있습니다. STAFF는 별도의 관리자 API로 주문 상태를 관리하며, READY 전환 시 고객에게 **인앱 알림과 FCM 푸시**가 동시에 발송됩니다. 또한 사용자는 본인의 전신 사진과 상품 이미지를 기반으로 **OpenAI gpt-image-1을 실제 연동한 AI 가상 피팅**을 사용할 수 있고, 모든 이미지는 S3에 저장돼 URL로 관리됩니다.

---

## 목차

1. [주요 기능](#1-주요-기능)
2. [기술 스택](#2-기술-스택)
3. [시스템 아키텍처](#3-시스템-아키텍처)
4. [핵심 도메인 / 테이블](#4-핵심-도메인--테이블)
5. [주요 API](#5-주요-api)
6. [API 문서 (Swagger)](#6-api-문서-swagger)
7. [로컬 실행 방법](#7-로컬-실행-방법)
8. [환경 변수](#8-환경-변수)
9. [데모 시나리오](#9-데모-시나리오)
10. [구현 포인트](#10-구현-포인트)

---

## 1. 주요 기능

### CUSTOMER

- 회원가입 / 로그인 (JWT 발급)
- 로그인 ID 중복 체크
- 마이페이지 조회 / 수정
- 프로필 이미지 업로드 (S3 multipart)
- 가상 착용용 전신 사진 업로드 (S3 multipart)
- FCM 토큰 등록 / 갱신
- 상품 목록 / 상품 상세 조회
- NFC 태그 UID 기반 상품·옵션 조회
- 상품 조회 이력 저장
- 장바구니 조회 / 추가 / 수량 변경 / 단건 삭제 / 전체 비우기
- 직접 주문 / 장바구니 주문 + Mock 결제
- 주문 목록 / 주문 상세 / 주문 취소
- 인앱 알림 조회
- AI 가상 피팅 요청 / 결과 조회 / 목록 조회

### STAFF

- STAFF 전용 관리자 API 접근 제어 (CUSTOMER 접근 차단)
- 관리자 홈 주문 요약
- 관리자 주문 목록 조회
- 관리자 주문 상세 조회 (주문자 이름·전화번호 포함)
- 주문 상태 변경 (PAID → PREPARING → READY → PICKED_UP)
- 관리자 주문 취소
- READY 전환 시 PICKUP_READY 알림 자동 생성
- READY 전환 시 FCM 푸시 자동 발송 (토큰 등록된 사용자 한정)

### AI Virtual Try-On

- 사용자의 전신 사진 + 상품 이미지를 입력으로 AI 합성
- **OpenAI gpt-image-1 실제 연동**
- 생성된 결과 이미지를 S3에 업로드해 영구 URL로 관리
- 상태 관리: `PENDING` / `PROCESSING` / `DONE` / `FAILED`
- 생성 후에는 try-on ID로 재조회 가능 (목록 조회도 지원)

---

## 2. 기술 스택

| 분류 | 내용 |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.5.9 |
| Security | Spring Security, JWT (HS256) |
| Database | MySQL 8 |
| Migration | Liquibase (YAML changelog) |
| API Docs | Swagger / springdoc-openapi |
| Storage | AWS S3 |
| AI | OpenAI gpt-image-1 |
| Push | Firebase Admin SDK / FCM |
| Infra | Docker, Docker Compose, AWS EC2 |
| Build | Gradle |

---

## 3. 시스템 아키텍처

```
            Android App
                │
                │  REST API (JSON) / Multipart Upload
                ▼
        ┌────────────────────┐
        │  Spring Boot       │
        │  (FitPick Backend) │
        └──────────┬─────────┘
                   │
   ┌───────────────┼────────────────┬─────────────────────┐
   │               │                │                     │
   ▼               ▼                ▼                     ▼
 MySQL 8        AWS S3        OpenAI Image API     Firebase Cloud Messaging
(트랜잭션      (프로필 / 가상   (gpt-image-1,        (READY 전환 시
 데이터)        착용 / 결과       AI 가상 피팅)         푸시 발송)
                이미지)
```

- **Android ↔ Spring Boot**: JWT 인증 기반 REST API 호출. 이미지 업로드는 multipart/form-data.
- **Spring Boot ↔ MySQL**: 사용자/상품/주문/알림/가상 피팅 등 모든 트랜잭션 데이터. 스키마 변경은 Liquibase changeset으로 관리.
- **Spring Boot ↔ S3**: 프로필, 가상 착용용 전신 사진, AI 생성 결과 이미지를 모두 S3에 저장하고 URL을 DB에 보관.
- **Spring Boot ↔ OpenAI**: 가상 피팅 요청 시 gpt-image-1 호출 → 응답 이미지를 다시 S3에 업로드.
- **Spring Boot ↔ FCM**: 주문 상태가 READY로 바뀌면 Firebase Admin SDK를 통해 등록된 디바이스 토큰으로 푸시 발송.

---

## 4. 핵심 도메인 / 테이블

| 테이블 | 역할 |
| --- | --- |
| `users` | 회원 정보, 역할(CUSTOMER/STAFF), 프로필·가상 착용 이미지 URL, FCM 토큰 |
| `clothes` | 상품(옷) 마스터 |
| `clothes_options` | 상품의 사이즈/색상 등 옵션, 옵션별 재고 |
| `clothes_images` | 상품 이미지 (대표/추가) |
| `nfc_tags` | NFC 태그 UID ↔ 상품(또는 옵션) 매핑. `clothes_option_id`로 옵션 단위 태깅 지원 |
| `carts` | 사용자 장바구니 |
| `cart_items` | 장바구니에 담긴 옵션과 수량 |
| `orders` | 주문 헤더, 상태(`PAID`/`PREPARING`/`READY`/`PICKED_UP`/`CANCELED`) |
| `order_items` | 주문 상세 항목 (옵션, 수량, 가격 스냅샷, 썸네일) |
| `notifications` | 인앱 알림 (PICKUP_READY 등) |
| `try_ons` | 가상 피팅 요청 단위, 상태/원본/결과 이미지 URL |
| `try_on_items` | 가상 피팅에 사용된 상품(옵션) 매핑 |
| `view_histories` | 사용자별 상품 조회 이력 |

---

## 5. 주요 API

> 모든 비즈니스 응답은 공통 포맷을 따릅니다.
>
> ```json
> { "code": "...", "message": "...", "data": ..., "timestamp": "..." }
> ```

### Auth

| Method | Path | 설명 |
| --- | --- | --- |
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 (accessToken + user 정보 반환) |
| GET  | `/api/v1/auth/check-login-id` | 로그인 ID 중복 체크 |

### User (My Page)

| Method | Path | 설명 |
| --- | --- | --- |
| GET   | `/api/v1/users/me` | 내 정보 조회 (주문 수·미확인 알림 수 포함) |
| PATCH | `/api/v1/users/me` | 내 정보 수정 (name/phone/height/weight/ageGroup/address) |
| POST  | `/api/v1/users/me/profile-image` | 프로필 이미지 업로드 (multipart) |
| POST  | `/api/v1/users/me/try-on-image`  | 가상 착용용 이미지 업로드 (multipart) |
| POST  | `/api/v1/users/me/fcm-token`     | FCM 토큰 등록 / 갱신 |

### Clothes / NFC

| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/v1/clothes` | 상품 목록 |
| GET | `/api/v1/clothes/{clothesId}` | 상품 상세 (옵션·이미지 포함) |
| GET | `/api/v1/clothes/nfc/{tagUid}` | NFC 태그 UID로 상품(옵션) 조회 |

### Cart

| Method | Path | 설명 |
| --- | --- | --- |
| GET    | `/api/v1/cart` | 장바구니 조회 |
| POST   | `/api/v1/cart/items` | 장바구니에 옵션 추가 (동일 옵션이면 수량 머지) |
| PATCH  | `/api/v1/cart/items/{cartItemId}` | 수량 변경 |
| DELETE | `/api/v1/cart/items/{cartItemId}` | 단건 삭제 |
| DELETE | `/api/v1/cart` | 장바구니 전체 비우기 |

### Order

| Method | Path | 설명 |
| --- | --- | --- |
| POST | `/api/v1/order/direct` | 직접 주문 + Mock 결제 |
| POST | `/api/v1/order/cart` | 장바구니 주문 + Mock 결제 (주문 후 장바구니 비움) |
| GET  | `/api/v1/order` | 내 주문 목록 |
| GET  | `/api/v1/order/{orderId}` | 주문 상세 |
| POST | `/api/v1/order/{orderId}/cancel` | 주문 취소 (재고 복원) |

> **주의**: 주문 취소는 `POST`입니다 (`PATCH` 아님).

### Try-On

| Method | Path | 설명 |
| --- | --- | --- |
| POST | `/api/v1/try-ons` | 가상 피팅 생성 (OpenAI 호출 → 결과 S3 업로드) |
| GET  | `/api/v1/try-ons/{tryOnId}` | 가상 피팅 단건 조회 |
| GET  | `/api/v1/try-ons` | 내 가상 피팅 목록 |

### Notification

| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/v1/notifications` | 내 알림 목록 |

### Admin (STAFF only)

| Method | Path | 설명 |
| --- | --- | --- |
| GET   | `/api/v1/admin/orders/summary` | 관리자 홈 주문 요약 |
| GET   | `/api/v1/admin/orders` | 관리자 주문 목록 |
| GET   | `/api/v1/admin/orders/{orderId}` | 관리자 주문 상세 (주문자 이름·전화번호 포함) |
| PATCH | `/api/v1/admin/orders/{orderId}/status` | 주문 상태 변경 (READY 시 알림·FCM 트리거) |

---

## 6. API 문서 (Swagger)

- Local Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Production Swagger UI: `http://<EC2_PUBLIC_IP>:8080/swagger-ui/index.html`

---

## 7. 로컬 실행 방법

> 사전 조건: Java 21, Docker Desktop 설치.

### 1) 저장소 클론

```bash
git clone <repository-url>
cd FitPick_BE
```

### 2) 로컬 MySQL 실행 (Docker Compose)

`compose.local.yaml`은 MySQL 8을 호스트의 **3307 포트**로 띄웁니다.

```bash
docker compose -f compose.local.yaml up -d
```

### 3) 환경 변수 설정

루트의 `.env.example`을 복사해 `.env`로 만들고, 추가로 필요한 키들을 채워주세요 (자세한 키는 [환경 변수](#8-환경-변수) 참고).

```bash
cp .env.example .env
```

> AI 가상 피팅과 FCM 푸시를 실제로 검증하려면 `OPENAI_API_KEY`, `FIREBASE_CREDENTIALS_PATH`(서비스 계정 JSON 경로)도 함께 설정해야 합니다. 키가 없으면 해당 기능만 비활성화/실패 상태가 됩니다.

### 4) 애플리케이션 실행

`local-mysql` 프로파일로 부트런합니다.

```bash
./gradlew bootRun --args='--spring.profiles.active=local-mysql'
```

### 5) Swagger 접속

브라우저에서 다음 주소를 엽니다.

```
http://localhost:8080/swagger-ui/index.html
```

---

## 8. 환경 변수

> 실제 값은 **절대 커밋하지 마세요**. 아래는 키 이름과 예시 자리표시자입니다.

```env
# JWT
JWT_SECRET=your-jwt-secret-min-32-chars
JWT_ACCESS_TOKEN_EXPIRATION_MS=86400000

# OpenAI (AI 가상 피팅)
OPENAI_API_KEY=your-openai-api-key

# AWS S3 (이미지 업로드) — 자격 증명은 IAM Role 또는 표준 AWS 자격 증명 체인 사용
AWS_REGION=ap-northeast-2
S3_BUCKET_NAME=your-s3-bucket-name

# Firebase (FCM)
FIREBASE_CREDENTIALS_PATH=path/to/firebase-service-account.json
FIREBASE_ENABLED=true

# MySQL (compose.prod.yaml에서 사용)
MYSQL_DATABASE=fitpick
MYSQL_USER=fitpick
MYSQL_PASSWORD=your-db-password
MYSQL_ROOT_PASSWORD=your-db-root-password
```

> EC2 운영 환경에서는 AWS 자격 증명을 환경 변수로 노출하지 않고 **IAM Role**로 부여하는 것을 권장합니다.

---

## 9. 데모 시나리오

1. **CUSTOMER 로그인** — 일반 사용자 계정으로 로그인하여 JWT 발급
2. **마이페이지에서 가상 착용용 전신 사진 확인** — 등록되어 있지 않다면 업로드
3. **NFC 태그 인식** — 매장에서 옷에 부착된 NFC 태그를 태깅해 상품·옵션 조회
4. **상품 상세 및 옵션 확인** — 사이즈/색상/재고 확인
5. **AI 가상 피팅 실행** — 내 전신 사진 + 상품 이미지로 합성 요청
6. **생성된 결과 이미지 확인** — `try-ons` 단건 조회로 결과 이미지 URL 확인
7. **장바구니 담기 또는 바로 주문** — Mock 결제 진행 → 주문 생성, 재고 차감
8. **STAFF 로그인** — 관리자 계정으로 로그인
9. **관리자 주문 상세 확인** — 주문자 이름·전화번호 포함
10. **주문 상태를 READY로 변경** — 인앱 알림 생성 + FCM 푸시 발송 트리거
11. **CUSTOMER가 인앱 알림 및 FCM 푸시 수신**
12. **STAFF가 PICKED_UP으로 변경** — 픽업 완료 처리

> 데모 계정은 seed 데이터로 구성됩니다. (`scripts/seed.sh` 참고)

---

## 10. 구현 포인트

- **JWT는 최소 인증 정보만 보관**
  payload에는 `sub(userId)` / `role` / `iat` / `exp`만 두고, 사용자 상세 정보(이름·키·주소·이미지 URL 등)는 로그인 응답 본문과 `/api/v1/users/me`로 분리.

- **공통 응답 포맷 통일**
  `ApiResponse<T>` 한 종류로 성공·에러를 모두 표현. 에러도 `{ code, message, data, timestamp }` 형태를 유지해 클라이언트 파싱이 일관됨.

- **GlobalExceptionHandler로 400 vs 500 명확화**
  잘못된 JSON 본문, enum 변환 실패, `@Valid` 위반, `ConstraintViolationException`, `MissingServletRequestParameterException` 등을 모두 400으로 정규화하고 내부 스택을 클라이언트에 노출하지 않음.

- **Liquibase 기반 스키마 이력 관리**
  변경은 항상 신규 changeset(`0019-...`, `0020-...` 식)으로 추가. 이미 적용된 changeset은 절대 수정하지 않음. FK가 걸린 컬럼의 UNIQUE 제거 같은 까다로운 변경은 atomic ALTER로 안전하게 처리(0018).

- **주문/재고 정합성**
  주문 생성 시 옵션별 재고를 트랜잭션 내에서 감소, 취소 시 복원. 가격은 주문 시점에 스냅샷으로 저장해 이후 상품 가격이 바뀌어도 영향 없음.

- **CUSTOMER / STAFF 권한 분리**
  Spring Security 레벨에서 `/api/v1/admin/**`은 STAFF만 접근 가능. CUSTOMER 접근은 403으로 차단.

- **NFC 옵션 단위 확장**
  초기 설계는 옷 단위 NFC였으나, 옵션 단위 태깅 요구가 생겨 `nfc_tags.clothes_option_id`를 nullable로 추가하고 NULL이면 레거시 옷 단위 태그로 폴백.

- **READY 전환 → 알림 + FCM 동시 처리**
  주문이 READY로 바뀌는 단일 트랜잭션 흐름에서 `notifications` insert와 Firebase Admin SDK 푸시를 함께 수행. 토큰이 없거나 발송 실패해도 알림 자체는 보존.

- **S3 기반 이미지 관리**
  프로필·가상 착용·AI 생성 이미지를 모두 S3에 업로드하고 DB에는 URL만 보관. 정적 자산이 애플리케이션과 분리되어 스케일 아웃에 영향 없음.

- **AI 가상 피팅 실제 연동**
  OpenAI `gpt-image-1`로 합성을 호출하고, 응답 이미지를 다시 S3에 업로드해 영구 URL을 만든 뒤 `try_ons`에 결과 URL을 저장. **외부 API 실패 시 상태를 `FAILED`로 전환**해 클라이언트가 재시도/안내 가능.

- **빌드/배포 일원화**
  Docker Compose(prod)로 MySQL + App을 함께 띄우고, Firebase 서비스 계정 JSON은 호스트 파일을 컨테이너에 read-only 마운트해 이미지에 비밀이 포함되지 않도록 분리.
