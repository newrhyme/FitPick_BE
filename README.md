# FitPick Backend

오프라인 스마트 의류 매장을 위한 **Android 기반 O2O 쇼핑 서비스**의 백엔드.
고객은 매장에서 옷에 부착된 **NFC 태그(옵션 단위)** 를 인식해 상품 정보를 즉시 확인하고, 장바구니/바로 주문을 통해 모의 결제 → 매장 픽업까지 진행합니다.
STAFF는 별도의 관리자 API로 주문 상태를 관리하며, `READY` 전환 시 고객에게 **인앱 알림 + FCM 푸시**가 동시에 발송됩니다.
또한 사용자는 본인의 전신 사진과 상품 이미지를 기반으로 **OpenAI gpt-image-1을 실제 연동한 AI 가상 피팅**을 사용할 수 있고, 모든 이미지는 S3에 저장되어 URL로 관리됩니다.

---

## 목차

1. [핵심 차별점](#1-핵심-차별점)
2. [주요 기능](#2-주요-기능)
3. [기술 스택](#3-기술-스택)
4. [시스템 아키텍처](#4-시스템-아키텍처)
5. [핵심 도메인 / 테이블](#5-핵심-도메인--테이블)
6. [주요 API](#6-주요-api)
7. [API 문서 (Swagger)](#7-api-문서-swagger)
8. [로컬 실행 방법](#8-로컬-실행-방법)
9. [환경 변수](#9-환경-변수)
10. [배포 & CI/CD](#10-배포--cicd)
11. [데모 계정 & 시나리오](#11-데모-계정--시나리오)
12. [구현 포인트](#12-구현-포인트)

---

## 1. 핵심 차별점

- **옵션 단위 NFC 태깅** — 같은 옷의 색상·사이즈 옵션마다 별도 NFC UID를 부여해 매장 진열 그대로의 컨텍스트로 즉시 인식. 옵션 매핑이 없는 태그는 자동으로 옷 단위로 폴백.
- **AI 가상 피팅 실제 연동** — OpenAI `gpt-image-1` 호출 → 배경 제거 + TPO 기반 새 배경 생성 → S3 업로드. 모의 응답이 아니라 외부 LLM 이미지 API에 실제 결제 키로 연결.
- **비동기 처리 + 즉시 응답 + 푸시 알림** — 가상 피팅 요청은 1초 안에 `PROCESSING`으로 응답하고, 백그라운드 전용 스레드풀(`tryOnExecutor`)에서 실제 처리. 완료 시 **인앱 알림 + FCM 푸시**로 결과를 통보해 클라이언트가 폴링 없이 결과를 받음.
- **통합 알림 인박스** — 주문 상태 변경(`ORDER`)과 가상 피팅 완료/실패(`TRYON`) 모두 **인앱 알림 + FCM 푸시**를 동시 발송. 사용자는 인박스 한 곳에서 모든 컨텍스트 알림을 확인하고, 알림 단위로 읽음 처리·딥링크가 가능.
- **운영 안전성** — JWT는 최소 정보만 담고, 공통 응답/에러 포맷 통일, 400/500 분기 명확화, Liquibase 기반 점진적 스키마 진화, IAM Role 기반 자격 관리, 비밀은 컨테이너에 read-only 마운트.
- **CI/CD 자동화** — `main` push 시 GitHub Actions가 EC2에서 `git pull` → `bootJar` → `docker compose up -d --build`까지 자동 수행.

---

## 2. 주요 기능

### CUSTOMER

- 회원가입 / 로그인 (JWT 발급) / 로그인 ID 중복 체크
- 마이페이지 조회·수정, 프로필·가상 착용 이미지 업로드 (S3 multipart)
- FCM 토큰 등록·갱신
- 상품 목록 / 상품 상세 / **NFC 태그 UID 기반 상품·옵션 조회**
- 상품 조회 이력 저장
- 장바구니 조회·추가·수량 변경·단건 삭제·전체 비우기
- 직접 주문 / 장바구니 주문 + Mock 결제, 주문 목록·상세·취소
- 인앱 알림 조회 / 단건·전체 읽음 처리 / 미확인 알림 개수 조회
- AI 가상 피팅 요청·결과 조회·목록 조회 (비동기)

### STAFF

- STAFF 전용 관리자 API 접근 제어 (CUSTOMER 접근 차단)
- 관리자 홈 주문 요약 / 주문 목록 / 주문 상세 (주문자 이름·전화번호 포함)
- 주문 상태 변경 `PAID → PREPARING → READY → PICKED_UP` — `comment` 필수
- 관리자 주문 취소 (재고 복원)
- `READY` 등 상태 전환 시 **인앱 알림(`notificationType=ORDER`) 자동 생성 + FCM 푸시** (토큰 등록된 사용자에 한해)

### AI Virtual Try-On

- 전신 사진 + 상품 이미지 → OpenAI `gpt-image-1` 합성 (출력 1024x1024)
- 배경 자동 제거 + **TPO(시간/장소/상황) 매칭 새 배경 자동 생성**
- 선택 입력 `style` (자유 텍스트) — 자동 TPO 매핑보다 우선 적용
- 상태 관리: `PENDING` / `PROCESSING` / `DONE` / `FAILED`
- **비동기 처리** — 요청 즉시 `PROCESSING`으로 응답, 전용 스레드풀(`tryOnExecutor`, core=2 / max=4 / queue=20)에서 실제 처리
- 완료/실패 시 **인앱 알림(`notificationType=TRYON`) + FCM 푸시 자동 발송** — `notifications.try_on_id` FK로 결과 화면에 딥링크 가능
- 결과 이미지를 S3에 업로드하여 영구 URL로 관리

---

## 3. 기술 스택

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
| CI/CD | GitHub Actions |
| Build | Gradle |

---

## 4. 시스템 아키텍처

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
   ┌───────────────────┼─────────────────┬──────────────────────────┐
   ▼                   ▼                 ▼                          ▼
 MySQL 8             AWS S3        OpenAI Image API      Firebase Cloud Messaging
(트랜잭션 데이터)   (프로필 / 가상    (gpt-image-1,             (READY · TRY_ON_DONE
                    착용 / 결과      AI 가상 피팅)              · TRY_ON_FAILED 푸시)
                    이미지)
```

- **Android ↔ Spring Boot** — JWT 인증 기반 REST API. 이미지 업로드는 multipart/form-data.
- **Spring Boot ↔ MySQL** — 사용자/상품/주문/알림/가상 피팅 등 모든 트랜잭션 데이터. 스키마 변경은 Liquibase changeset.
- **Spring Boot ↔ S3** — 프로필·가상 착용·AI 결과 이미지 영구 저장. DB에는 URL만 보관.
- **Spring Boot ↔ OpenAI** — 가상 피팅 시 `gpt-image-1` 호출. **전용 스레드풀에서 비동기 수행**, 클라이언트에는 즉시 `PROCESSING` 응답.
- **Spring Boot ↔ FCM** — 주문 상태 변경 / 가상 피팅 완료·실패 시 Firebase Admin SDK로 푸시 발송.

---

## 5. 핵심 도메인 / 테이블

| 테이블 | 역할 |
| --- | --- |
| `users` | 회원 정보, 역할(CUSTOMER/STAFF), 프로필·가상 착용 이미지 URL, FCM 토큰 |
| `clothes` | 상품(옷) 마스터 |
| `clothes_options` | 상품의 사이즈/색상 등 옵션, 옵션별 재고 |
| `clothes_images` | 상품 이미지 (대표·추가) |
| `nfc_tags` | NFC 태그 UID ↔ 상품(또는 옵션) 매핑. `clothes_option_id`로 옵션 단위 태깅 지원 |
| `carts` / `cart_items` | 사용자 장바구니 / 장바구니에 담긴 옵션·수량 |
| `orders` | 주문 헤더, 상태(`PAID`/`PREPARING`/`READY`/`PICKED_UP`/`CANCELED`) |
| `order_items` | 주문 상세 항목 (옵션, 수량, 가격 스냅샷, 썸네일) |
| `notifications` | 인앱 알림 (`notification_type` = `ORDER` / `TRYON`). `order_id` 또는 `try_on_id` FK로 상세 화면 딥링크 |
| `try_ons` | 가상 피팅 요청 단위. 상태/원본/결과 이미지 URL, `style`(배경/분위기 자유 텍스트) |
| `try_on_items` | 가상 피팅에 사용된 상품(옵션) 매핑 |
| `view_histories` | 사용자별 상품 조회 이력 |

> 주문 상태 변경(`ORDER`)과 가상 피팅 완료/실패(`TRYON`)는 모두 `notifications` 테이블에 저장되며 `/api/v1/notifications`에 통합 노출됩니다.
> Liquibase `0021-migrate-notification-type-to-order-tryon`이 적용되어 기존 `PICKUP_READY` 행은 `ORDER`로 마이그레이션되었습니다.

---

## 6. 주요 API

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
| POST | `/api/v1/try-ons` | 가상 피팅 생성 (즉시 `PROCESSING` 응답, 백그라운드에서 OpenAI 호출·S3 업로드) |
| GET  | `/api/v1/try-ons/{tryOnId}` | 가상 피팅 단건 조회 |
| GET  | `/api/v1/try-ons` | 내 가상 피팅 목록 |

### Notification

| Method | Path | 설명 |
| --- | --- | --- |
| GET   | `/api/v1/notifications` | 내 알림 목록 (`isRead=false`만 반환, `ORDER` · `TRYON` 모두 노출) |
| GET   | `/api/v1/notifications/unread-count` | 미확인 알림 개수 (레드닷 표시용) |
| PATCH | `/api/v1/notifications/{notificationId}/read` | 단건 읽음 처리 (idempotent) |
| PATCH | `/api/v1/notifications/read-all` | 전체 읽음 처리 |
| POST  | `/api/v1/notifications/test-fcm` | 본인의 FCM 토큰으로 테스트 푸시 (디바이스 등록 확인용) |

### Admin (STAFF only)

| Method | Path | 설명 |
| --- | --- | --- |
| GET   | `/api/v1/admin/orders/summary` | 관리자 홈 주문 요약 |
| GET   | `/api/v1/admin/orders` | 관리자 주문 목록 |
| GET   | `/api/v1/admin/orders/{orderId}` | 관리자 주문 상세 (주문자 이름·전화번호 포함) |
| PATCH | `/api/v1/admin/orders/{orderId}/status` | 주문 상태 변경 (`comment` 필수, 알림·FCM body로 사용) |

---

## 7. API 문서 (Swagger)

- Local Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Production Swagger UI: `http://<EC2_PUBLIC_IP>:8080/swagger-ui/index.html`

---

## 8. 로컬 실행 방법

> 사전 조건: Java 21, Docker Desktop.

### 1) 저장소 클론

```bash
git clone <repository-url>
cd FitPick_BE
```

### 2) 로컬 MySQL 실행

`compose.local.yaml`은 MySQL 8을 호스트의 **3307 포트**로 띄웁니다.

```bash
docker compose -f compose.local.yaml up -d
```

### 3) 환경 변수 설정

`.env.example`을 복사해 `.env`로 만들고 필요한 키를 채웁니다 (자세한 키는 [환경 변수](#9-환경-변수) 참고).

```bash
cp .env.example .env
```

> AI 가상 피팅과 FCM 푸시를 실제로 검증하려면 `OPENAI_API_KEY`, `FIREBASE_CREDENTIALS_PATH`(서비스 계정 JSON 경로)도 함께 설정해야 합니다. 키가 없으면 해당 기능만 비활성화/실패 상태로 동작합니다.

### 4) 애플리케이션 실행

`local-mysql` 프로파일로 부트런:

```bash
./gradlew bootRun --args='--spring.profiles.active=local-mysql'
```

### 5) Swagger 접속

```
http://localhost:8080/swagger-ui/index.html
```

---

## 9. 환경 변수

> 실제 값은 **절대 커밋하지 마세요**. 아래는 키 이름과 자리표시자입니다.

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

# MySQL (compose.prod.yaml 에서 사용)
MYSQL_DATABASE=fitpick
MYSQL_USER=fitpick
MYSQL_PASSWORD=your-db-password
MYSQL_ROOT_PASSWORD=your-db-root-password
```

> EC2 운영 환경에서는 AWS 자격 증명을 환경 변수로 노출하지 않고 **IAM Role**로 부여하는 것을 권장합니다.

---

## 10. 배포 & CI/CD

### 운영 환경 구성

- AWS EC2(Ubuntu) 한 대에 `compose.prod.yaml` 기반 Docker Compose 스택으로 운영.
- 컨테이너 구성
  - `fitpick-app` — Spring Boot 컨테이너, `8080:8080` 바인딩
  - `fitpick-mysql` — MySQL 8 컨테이너. 호스트 포트는 `127.0.0.1:3306` 으로만 열려 있어 **외부 인터넷에서는 접근 불가**. 로컬 DB 클라이언트 접속은 SSH 터널 경유.
- Firebase 서비스 계정 JSON은 호스트 파일 `~/FitPick_BE/firebase-key.json`을 컨테이너에 **read-only 볼륨 마운트**.
- 이미지 정적 자산은 모두 S3 (`fitpick-images`, `ap-northeast-2`)로 분리. 자격 증명은 EC2 IAM Role.

### SSH 터널로 DB 접속 (예시)

로컬 DB 클라이언트(DB Browser, DBeaver 등)에서 EC2의 MySQL에 직접 붙어야 할 때:

```bash
# 로컬 3306이 비어 있을 때
ssh -i ~/.ssh/<your-key> -N -L 3306:127.0.0.1:3306 ubuntu@<EC2_PUBLIC_IP>

# 로컬 3306이 이미 쓰이면 다른 포트(예: 13306)
ssh -i ~/.ssh/<your-key> -N -L 13306:127.0.0.1:3306 ubuntu@<EC2_PUBLIC_IP>
```

DB 클라이언트는 `127.0.0.1:3306`(또는 `13306`)으로 접속.

### CI

`.github/workflows/ci.yml` — 모든 `main` / `develop` push 및 PR에서:

- Java 21 셋업 → `./gradlew clean build -x test`

### CD

`.github/workflows/deploy.yml` — `main` push 시 자동 배포:

1. `appleboy/ssh-action` 으로 EC2에 SSH 접속
2. `git pull origin main`
3. `./gradlew clean bootJar -x test`
4. `docker compose -f compose.prod.yaml --env-file .env up -d --build`
5. `docker ps`로 컨테이너 상태 출력

EC2의 working tree에 unstaged 변경이 있으면 `git pull`이 실패하므로, 운영 서버 파일은 직접 수정하지 않고 항상 로컬 → push → 자동 pull 흐름으로 변경.

---

## 11. 데모 계정 & 시나리오

### 데모 계정 (모든 계정 비밀번호: `pass01`)

- CUSTOMER: `customer01` ~ `customer05`
- STAFF: `staff01` ~ `staff03`

추천 데모 사용자: `customer04` (try-on 이미지 등록되어 있고, READY 주문 + 미확인 `ORDER` 알림 보유)
추천 STAFF 사용자: `staff01`

### 시연 시나리오

1. **CUSTOMER 로그인** — JWT 발급
2. **마이페이지** — 가상 착용용 전신 사진 확인 (미등록 시 업로드)
3. **NFC 태그 인식** — 옷에 부착된 태그(옵션 단위) 태깅 → 상품·옵션 조회
4. **상품 상세 및 옵션 확인** — 사이즈/색상/재고 확인
5. **AI 가상 피팅 실행** — 전신 사진 + 상품 이미지로 합성 요청 (즉시 `PROCESSING` 응답)
6. **결과 수신** — FCM 푸시 도착 + 알림함에 `TRYON` 알림 적재 → `try-ons/{tryOnId}` 단건 조회로 결과 이미지 확인
7. **장바구니 담기 또는 바로 주문** — Mock 결제 → 주문 생성, 재고 차감
8. **STAFF 로그인** — 관리자 계정
9. **관리자 주문 상세 확인** — 주문자 이름·전화번호
10. **주문 상태를 `READY`로 변경** — `comment` 입력 → 인앱 알림 생성 + FCM 푸시 발송
11. **CUSTOMER 알림 수신** — 인앱 알림 + FCM. 단건/전체 읽음 처리
12. **STAFF가 `PICKED_UP`으로 변경** — 픽업 완료

---

## 12. 구현 포인트

- **JWT 최소 페이로드**
  `sub(userId)` / `role` / `iat` / `exp` 만 보관. 사용자 상세 정보는 로그인 응답 본문과 `/api/v1/users/me`로 분리하여 토큰 비대화 / 정보 비공개 측면에서 안전.

- **공통 응답 포맷 통일**
  성공·에러를 모두 `ApiResponse<T>` 한 종류로 표현. 에러도 `{ code, message, data, timestamp }` 형태를 유지해 클라이언트 파싱이 일관됨.

- **GlobalExceptionHandler로 400 vs 500 명확화**
  잘못된 JSON 본문, enum 변환 실패, `@Valid` 위반, `ConstraintViolationException`, `MissingServletRequestParameterException` 등을 모두 400으로 정규화하고 내부 스택은 클라이언트에 노출하지 않음.

- **Liquibase 기반 스키마 이력 관리**
  변경은 항상 신규 changeset(`0019-...`, `0020-...` 식)으로 추가. 이미 적용된 changeset은 절대 수정하지 않음. FK가 걸린 컬럼의 UNIQUE 제거 같은 까다로운 변경은 atomic ALTER로 안전하게 처리(0018).

- **주문/재고 정합성**
  주문 생성 시 옵션별 재고를 트랜잭션 내에서 감소, 취소 시 복원. 가격은 주문 시점에 스냅샷으로 저장해 이후 상품 가격이 바뀌어도 영향 없음.

- **CUSTOMER / STAFF 권한 분리**
  Spring Security 레벨에서 `/api/v1/admin/**`은 STAFF만 접근 가능. CUSTOMER 접근은 403으로 차단.

- **NFC 옵션 단위 확장**
  초기 설계는 옷 단위 NFC였으나, 옵션 단위 태깅 요구가 생겨 `nfc_tags.clothes_option_id`를 nullable로 추가하고 NULL이면 레거시 옷 단위 태그로 폴백.

- **주문 상태 변경 → 알림 + FCM 동시 처리**
  관리자가 주문 상태를 변경하면 `comment` 필드를 본문으로 한 알림이 `notifications`에 저장되고 Firebase Admin SDK로 푸시가 발송됨. 토큰이 없거나 발송 실패해도 알림 자체는 보존.

- **가상 피팅 비동기 파이프라인**
  컨트롤러는 `PROCESSING` 행만 `REQUIRES_NEW`로 즉시 커밋하고 클라이언트에는 곧바로 응답. 실제 OpenAI 호출·S3 업로드·DB 업데이트·알림 발송은 전용 스레드풀(`tryOnExecutor`, core=2 / max=4 / queue=20)에서 백그라운드 수행. 완료/실패 시 `notifications` 테이블(`type=TRYON`, `try_on_id` FK)에 행을 적재하고 FCM 푸시를 보내, 폴링 없이도 인박스/푸시 양쪽으로 결과 도착을 인지할 수 있게 설계.

- **통합 알림 인박스 (`ORDER` / `TRYON`)**
  `notification_type`을 `ORDER`(주문 상태 변경) / `TRYON`(가상 피팅 완료·실패) 2종으로 단순화. 두 종류 모두 **DB 저장 + FCM 푸시**가 동시에 일어나고 `/api/v1/notifications`에 통합 노출. `notifications.order_id` 또는 `try_on_id` FK로 알림에서 상세 화면으로 바로 딥링크 가능.

- **AI 가상 피팅 실제 연동**
  OpenAI `gpt-image-1`로 합성 호출, 응답 이미지를 다시 S3에 업로드해 영구 URL 생성. 프롬프트에 **얼굴/체형 보존 규칙 + 배경 자동 제거 + TPO 매칭 새 배경 생성** 지침을 명시. 사용자가 `style`을 직접 입력하면 자동 TPO보다 우선 적용. 외부 API 실패 시 상태를 `FAILED`로 전환 + 푸시 발송.

- **S3 기반 이미지 관리**
  프로필·가상 착용·AI 생성 이미지를 모두 S3에 업로드하고 DB에는 URL만 보관. 정적 자산이 애플리케이션과 분리되어 스케일 아웃에 영향 없음. EC2는 IAM Role로 자격 증명 — 환경 변수에 키를 두지 않음.

- **빌드/배포 일원화 + CI/CD**
  Docker Compose(prod)로 MySQL + App을 함께 띄우고, Firebase 서비스 계정 JSON은 호스트 파일을 컨테이너에 read-only 마운트해 이미지에 비밀이 포함되지 않도록 분리. `main` push 시 GitHub Actions가 EC2에서 자동 pull/build/restart.

- **운영 안전 장치**
  운영 MySQL은 호스트의 `127.0.0.1:3306` 으로만 노출(외부 인터넷 차단), 원격 접근이 필요할 때만 SSH 터널을 사용. 비밀번호는 명령 줄에 노출하지 않고 `MYSQL_PWD` 환경 변수로 전달.
