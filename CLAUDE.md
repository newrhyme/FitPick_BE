FitPick Backend Guide

This file is the source of truth for AI coding agents working on the FitPick_BE project.

Before making any code changes, read this document carefully and follow the existing project conventions.

⸻

0. Project Overview

FitPick is an Android-based O2O shopping service for an offline smart clothing store.

The core user flows are:

CUSTOMER Flow

Login
→ Home / Product List
→ NFC Tagging (option-level)
→ Product Detail
→ Option Selection
→ Cart or Direct Purchase
→ Mock Payment
→ Order Created
→ Notification Check (in-app + FCM push)
→ In-store Pickup

STAFF Flow

Login
→ Admin Home
→ Order List
→ Order Detail
→ Update Order Status
→ Create Customer Notification
→ Mark Order as Picked Up

Optional CUSTOMER flow:

My Page → Profile Image Upload
My Page → Try-On Image Upload
AI Virtual Try-On (OpenAI gpt-image-1)

⸻

1. Tech Stack

* Java 21
* Spring Boot 3.5.9
* Spring Security
* JWT
* MySQL 8
* Liquibase YAML changelog
* Swagger / springdoc-openapi
* Docker / Docker Compose
* AWS EC2 deployment
* AWS S3 (profile / try-on image upload)
* OpenAI gpt-image-1 (AI virtual try-on)
* Firebase Admin SDK (FCM push)

⸻

2. Project Structure Convention

The base package is:

com.fitpick

The project follows a domain-oriented layered architecture.

Current domains:

domain/auth
domain/user
domain/clothes
domain/cart
domain/order
domain/notification
domain/nfc
domain/viewhistory
domain/tryon
domain/health
domain/sample
global/common
global/exception
global/security
global/config
global/logging
global/infra/s3
global/infra/openai
global/infra/firebase

The `global/config/AsyncConfig` defines the `tryOnExecutor` ThreadPoolTaskExecutor used by `TryOnAsyncProcessor`.

Services generally follow the interface + implementation pattern.

Example:

OrderService
OrderServiceImpl

DTOs must be separated into request DTOs and response DTOs.

All business API responses must follow the common response format:

{
"code": "...",
"message": "...",
"data": ...,
"timestamp": "..."
}

The /health endpoint is an exception and may return a simple infrastructure response.

⸻

3. Authentication and Authorization Policy

The main roles are:

CUSTOMER
STAFF

JWT payload must contain only minimal authentication and authorization information.

Recommended JWT payload:

{
"sub": "userId",
"role": "CUSTOMER",
"iat": 0,
"exp": 0
}

Do not put the following data into JWT payload:

name
phone
address
height
weight
cart
orders
profileImageUrl
tryOnImageUrl
fcmToken

User information should be returned in the response body, not inside the JWT.

The login response returns both accessToken and user information.

The signup response does not return a token. Signup only creates an account.

⸻

4. Completed Features

Auth / User

Completed:

* Signup
* Login
* Login ID duplicate check (GET /api/v1/auth/check-login-id)
* JWT issuance
* Login response includes user information
* AgeGroup enum applied
* GET /api/v1/users/me
* PATCH /api/v1/users/me (profile update)
* POST /api/v1/users/me/profile-image (multipart, S3)
* POST /api/v1/users/me/try-on-image (multipart, S3)
* POST /api/v1/users/me/fcm-token (FCM token register / refresh)
* users.try_on_image_url column added
* users.fcm_token column added
* My Page user summary response

AgeGroup values:

TEENS
TWENTIES
THIRTIES
FORTIES_PLUS

GET /api/v1/users/me response fields:

userId
loginId
name
role
phone
height
weight
ageGroup
address
profileImageUrl
tryOnImageUrl
hasTryOnImage
orderCount
unreadNotificationCount

PATCH /api/v1/users/me editable fields:

name
phone
height
weight
ageGroup
address

PATCH /api/v1/users/me does NOT update these fields:

loginId
role
password
profileImageUrl
tryOnImageUrl
fcmToken

Image / FCM token mutations use dedicated endpoints above.

⸻

Clothes / NFC

Completed:

* Product list
* Product detail
* Product lookup by NFC tag UID
* Product options
* Product images
* View history recording
* NFC tags extended to option-level (clothes_option_id, nullable for legacy fallback)

NFC test tag UIDs:

04A1B2C301
04A1B2C302
04A1B2C303
04A1B2C304
04A1B2C305
04A1B2C309
04A1B2C310

NFC lookup endpoint:

GET /api/v1/clothes/nfc/{tagUid}

If nfc_tags.clothes_option_id is NULL the tag is treated as a legacy clothes-level tag.

⸻

Cart

Completed:

* Get cart
* Add item to cart
* Update item quantity
* Delete cart item
* Clear entire cart (DELETE /api/v1/cart)
* Merge quantity when the same option is added again
* Prevent adding quantity over available stock

⸻

Order

Completed:

* Direct order
* Cart-based order
* Mock payment
* Stock decrease when an order is created
* Stock restore when an order is canceled
* Cart cleanup after cart-based order
* Order list
* Order detail
* Customer order cancel
* OrderItem response includes clothesId (for navigating to product detail)
* OrderItem response includes thumbnailImageUrl

Customer order APIs:

POST /api/v1/order/cart
POST /api/v1/order/direct
GET  /api/v1/order
GET  /api/v1/order/{orderId}
POST /api/v1/order/{orderId}/cancel

Important:

GET /api/v1/order/me does not exist.
Use GET /api/v1/order for the customer's order list.

Cancel endpoint is POST (not PATCH).

Order statuses:

PAID
PREPARING
READY
PICKED_UP
CANCELED

⸻

Admin / Staff

Completed:

* STAFF-only admin APIs
* Admin order list
* Admin order detail (includes customer name and phone)
* Admin order status update
* Admin order cancel
* Admin home order summary
* CUSTOMER access to /api/v1/admin/** is blocked

Valid status transition flow:

PAID -> PREPARING
PREPARING -> READY
READY -> PICKED_UP

When an order is changed to READY, a notification (type=ORDER) is created for the customer with the admin-provided comment as its body.
At this point an FCM push may be sent if the user has a registered fcm_token (see FCM section below).

⸻

Notification

Completed:

* Notification persistence (notifications table now has try_on_id FK + image_url)
* Customer notification list (GET /api/v1/notifications) — returns only isRead=false
* Unread count (GET /api/v1/notifications/unread-count) — for red-dot badge
* Mark single as read (PATCH /api/v1/notifications/{notificationId}/read) — idempotent
* Mark all as read (PATCH /api/v1/notifications/read-all)
* Order status change notification + FCM push (notification_type=ORDER; admin status change requires a "comment" field; used as FCM body)
* Try-on completion notification + FCM push (notification_type=TRYON, tryOnId set on the row)
* Try-on failure notification + FCM push (notification_type=TRYON, tryOnId set, hardcoded title/body)
* FCM token persistence (users.fcm_token)
* FCM test send API (POST /api/v1/notifications/test-fcm) — retained as device-token verification utility
* Firebase Admin SDK integrated (FcmService)

NotificationType values:

ORDER       — all order status-change pushes (covers what used to be PICKUP_READY)
TRYON       — both try-on completion and failure

(Pre-0021 the enum was PICKUP_READY / TRY_ON_DONE / TRY_ON_FAILED; changeset 0021 collapsed it to ORDER / TRYON and migrated existing rows from PICKUP_READY to ORDER.)

Status:

In-app notifications via the notifications table are fully wired end-to-end for BOTH order status changes (ORDER) AND try-on completion/failure (TRYON).
All triggers persist a row in notifications AND fire an FCM push — so they all appear in GET /api/v1/notifications and contribute to unread-count.
For TRYON rows the notifications.try_on_id FK is set; clients can navigate to GET /api/v1/try-ons/{tryOnId} for the generated image URL.
FCM real-send is integrated through FcmService for all triggers above.
The /test-fcm endpoint is kept as a device-token verification utility (sends a push to the caller's own fcm_token; persists a row with type=ORDER so the client can mark it as read).

⸻

Try-On (AI Virtual Try-On)

Completed:

* try_ons table + status flow (PENDING / PROCESSING / DONE / FAILED)
* try_ons.style column (free-text background/mood override, max 200 chars)
* POST /api/v1/try-ons (create try-on request — returns PROCESSING immediately)
* GET  /api/v1/try-ons/{tryOnId}
* GET  /api/v1/try-ons (my list)
* OpenAI gpt-image-1 integration (OpenAiImageClient)
* Prompt branching by English color / category labels
* Background removed + TPO-matched new background generation
* style field overrides automatic TPO mapping when provided
* Async processing via dedicated thread pool (tryOnExecutor, core=2 / max=4 / queue=20)
* On DONE: notifications row (type=TRYON, tryOnId set) + FCM push (data includes tryOnId, notificationId, generatedImageUrl)
* On FAILED: notifications row (type=TRYON, tryOnId set, hardcoded failure title/body) + FCM push
* Output image size: 1024x1024 (reduced from 1024x1536 for faster response)

Async flow:

1. Controller saves a PROCESSING row in REQUIRES_NEW so the row is committed immediately
2. asyncProcessor.process(...) runs in tryOnExecutor — OpenAI call → S3 upload → DB DONE/FAILED → persist notifications row + FCM push
3. Response is returned right away with status=PROCESSING and generatedImageUrl=null
4. Client receives the FCM push and/or sees a new entry in GET /api/v1/notifications (type=TRYON); re-fetches GET /api/v1/try-ons/{tryOnId} for the final image
5. The notifications row's try_on_id FK lets the client deep-link to the try-on detail without an extra lookup

Role separation:

users.try_on_image_url — default full-body image registered by the user
try_ons.original_image_url — snapshot of the original image for a specific request
try_ons.generated_image_url — AI-generated result image
try_ons.status — try-on processing status
try_ons.style — optional background/mood override (overrides auto TPO)

⸻

Image Upload (S3)

Completed:

* S3 uploader (global/infra/s3)
* POST /api/v1/users/me/profile-image (multipart)
* POST /api/v1/users/me/try-on-image (multipart)

⸻

Seed / Demo Data

The deployment server already contains demo data.

Accounts:

customer01 ~ customer05
staff01 ~ staff03

All passwords:

pass01

Demo data:

7 clothes
20 options
6 orders with different statuses
1 READY notification
customer04 has tryOnImageUrl
some other customers have null tryOnImageUrl

Seed script:

scripts/seed.sh

⸻

5. Deployment Server Information

Swagger:

http://3.39.34.202:8080/swagger-ui/index.html

Base URL:

http://3.39.34.202:8080

EC2 SSH:

ssh -i C:\Users\SSAFY\.ssh\fitpick-key.pem ubuntu@3.39.34.202

Project path on server:

~/FitPick_BE

App logs:

docker logs --tail=200 -f fitpick-app

Or:

docker compose -f compose.prod.yaml logs --tail=200 -f app

Required server env (compose.prod.yaml passes these through):

OPENAI_API_KEY                 (AI virtual try-on)
AWS S3 credentials / bucket    (image upload)
Firebase service account JSON  (FCM)
MYSQL_HOST_PORT                (local port collision avoidance)

MySQL host port binding on EC2:

compose.prod.yaml binds the mysql container to 127.0.0.1:3306 only.
This means MySQL is reachable from the EC2 host (e.g. SSH-tunneled DB client) but NOT from the public internet.
Do NOT change this to 0.0.0.0:3306 — security group is not the only safeguard.

Local DB client access via SSH tunnel (example):

ssh -i <key> -N -L 3306:127.0.0.1:3306 ubuntu@3.39.34.202
# then connect DB client to localhost:3306

CI/CD:

.github/workflows/ci.yml      — runs on push to main/develop and PRs: ./gradlew clean build -x test
.github/workflows/deploy.yml  — runs on push to main: SSH into EC2 → git pull origin main →
                                ./gradlew clean bootJar -x test →
                                docker compose -f compose.prod.yaml --env-file .env up -d --build

Important: if the EC2 working tree has unstaged modifications, git pull will fail and the deploy job aborts.
Never scp/edit files directly on EC2 — always go through local → commit → push so the next deploy stays clean.

⸻

6. Critical Warnings

Existing clothes data must be preserved.

Do not do the following unless explicitly instructed:

Do not delete existing clothes.
Do not delete existing clothes_images.
Do not delete existing nfc_tags.
Do not delete all existing clothes_options.
Do not rerun the old clothes seed SQL blindly.
Do not insert orders/order_items manually without understanding service logic.
Do not modify already-applied Liquibase changesets.

For Liquibase:

Never modify an already-applied changeset.
Always add a new changeset for schema changes.

Currently applied changesets:

0001-initial-schema
0016-add-try-on-image-url-to-users
0017-extend-try-ons
0018-extend-nfc-tags-to-options
0019-extend-notifications-for-try-on (adds notifications.try_on_id FK + image_url)
0020-add-style-to-try-ons (adds try_ons.style)
0021-migrate-notification-type-to-order-tryon (UPDATE notifications SET notification_type='ORDER' WHERE notification_type='PICKUP_READY')

Add new changesets as 0022-..., 0023-..., etc.

Order data should be created through APIs whenever possible.

Reason:

Stock decrease
Price snapshot
Cart cleanup
Stock restore on cancellation
Notification creation when READY
FCM push on READY (when token is registered)

These behaviors are implemented in service logic, so bypassing APIs may break consistency.

⸻

7. Remaining Candidate Features

1. End-to-End Demo Flow Check

This has the highest priority.

CUSTOMER checklist:

Login
Product list
NFC lookup (option-level)
Product detail
Add to cart
Get cart
Create order
Order list
Order detail (clothesId, thumbnailImageUrl visible)
Cancel order
Notification list
My Page
Profile / try-on image upload
AI virtual try-on

STAFF checklist:

Login
Admin home summary
Admin order list
Admin order detail (customer name + phone visible)
Update order status
Cancel order (admin)
Notification created when READY
FCM push delivered when READY (if fcm_token present)
Mark order as PICKED_UP

⸻

2. FCM End-to-End Polish

FCM is integrated but still benefits from:

Verification that READY transition reliably triggers push for users with fcm_token
Verification that TRY_ON_DONE / TRY_ON_FAILED pushes reach the device (no notifications-table fallback)
Handling of TOKEN_EMPTY / FCM_DISABLED / network failure paths in production logs

⸻

3. Swagger Error Documentation

Not all domains have full @ApiResponses documentation.

This improves documentation quality, but it is not critical for the demo flow.

⸻

8. Recommended Priority

If the demo is close, follow this order:

1. End-to-End demo flow check (CUSTOMER + STAFF)
2. Fix any field or response gaps surfaced by the E2E run
3. FCM READY-push + TRY_ON_DONE/FAILED-push verification on the live server
4. Swagger error documentation

⸻

9. Required Working Process

Before modifying code, first analyze the current structure.

Check the following:

Controller paths
DTO structure
Service interface/implementation structure
Repository methods
SuccessCode / ErrorCode structure
SecurityConfig authorization rules
Swagger configuration
Liquibase changelog structure

Before coding, report:

1. Files inspected
2. Files to modify
3. Reason for each change
4. Expected API behavior after the change
5. Verification plan

Do not create a new architecture if an existing project pattern already exists.

Follow the current project convention first.

⸻

10. Verification

After code changes, run:

./gradlew clean build

If possible, test the changed APIs through Swagger or curl.

After deployment, check logs:

docker logs --tail=200 -f fitpick-app

⸻

11. Response and Error Handling Rules

All business API responses must follow:

{
"code": "...",
"message": "...",
"data": ...,
"timestamp": "..."
}

Validation or malformed request errors must return HTTP 400, not HTTP 500.

Security errors must also follow the common response structure:

401 Unauthorized
403 Forbidden

Do not expose internal exception details to clients.

⸻

12. Important API Path Rules

Customer order list:

GET /api/v1/order

Do not use:

GET /api/v1/order/me

Customer order cancel (note: POST, not PATCH):

POST /api/v1/order/{orderId}/cancel

Cart:

GET    /api/v1/cart
POST   /api/v1/cart/items
PATCH  /api/v1/cart/items/{cartItemId}
DELETE /api/v1/cart/items/{cartItemId}
DELETE /api/v1/cart

Notifications:

GET   /api/v1/notifications                          (returns only isRead=false; both ORDER and TRYON rows are visible)
GET   /api/v1/notifications/unread-count
PATCH /api/v1/notifications/{notificationId}/read    (idempotent)
PATCH /api/v1/notifications/read-all
POST  /api/v1/notifications/test-fcm                 (device-token verification — sends a push to the caller's own fcm_token)

My Page:

GET   /api/v1/users/me
PATCH /api/v1/users/me
POST  /api/v1/users/me/profile-image     (multipart)
POST  /api/v1/users/me/try-on-image      (multipart)
POST  /api/v1/users/me/fcm-token

Try-on:

POST /api/v1/try-ons
GET  /api/v1/try-ons
GET  /api/v1/try-ons/{tryOnId}

Auth:

POST /api/v1/auth/signup
POST /api/v1/auth/login
GET  /api/v1/auth/check-login-id

Admin orders:

GET   /api/v1/admin/orders
GET   /api/v1/admin/orders/{orderId}
PATCH /api/v1/admin/orders/{orderId}/status
GET   /api/v1/admin/orders/summary

⸻

13. Demo Accounts

CUSTOMER accounts:

customer01 / pass01
customer02 / pass01
customer03 / pass01
customer04 / pass01
customer05 / pass01

STAFF accounts:

staff01 / pass01
staff02 / pass01
staff03 / pass01

Recommended demo users:

customer04

Reason:

customer04 has tryOnImageUrl.
customer04 has a READY order.
customer04 has one unread PICKUP_READY notification.

Recommended staff user:

staff01

⸻

14. Final Rule

When in doubt, prioritize demo stability over adding new features.

Do not break existing working flows.

The most important demo flow is:

NFC lookup
-> Product detail
-> Cart or direct order
-> Mock payment
-> Staff status update
-> Customer notification (in-app + FCM)
-> Pickup
