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
→ NFC Tagging
→ Product Detail
→ Option Selection
→ Cart or Direct Purchase
→ Mock Payment
→ Order Created
→ Notification Check
→ In-store Pickup

STAFF Flow

Login
→ Admin Home
→ Order List
→ Order Detail
→ Update Order Status
→ Create Customer Notification
→ Mark Order as Picked Up

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

⸻

2. Project Structure Convention

The base package is:

com.fitpick

The project follows a domain-oriented layered architecture.

Expected domains:

domain/auth
domain/user
domain/clothes
domain/cart
domain/order
domain/notification
domain/nfc
domain/viewhistory
global/common
global/exception
global/security

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

User information should be returned in the response body, not inside the JWT.

The login response returns both accessToken and user information.

The signup response does not return a token. Signup only creates an account.

⸻

4. Completed Features

Auth / User

Completed:

* Signup
* Login
* JWT issuance
* Login response includes user information
* AgeGroup enum applied
* GET /api/v1/users/me
* users.try_on_image_url column added
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

⸻

Clothes / NFC

Completed:

* Product list
* Product detail
* Product lookup by NFC tag UID
* Product options
* Product images
* View history recording

NFC test tag UIDs:

04A1B2C301
04A1B2C302
04A1B2C303
04A1B2C304
04A1B2C305
04A1B2C309
04A1B2C310

⸻

Cart

Completed:

* Get cart
* Add item to cart
* Update item quantity
* Delete cart item
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
* Order list
* Order detail

Customer order APIs:

GET /api/v1/order
GET /api/v1/order/{orderId}
PATCH /api/v1/order/{orderId}/cancel

Important:

GET /api/v1/order/me does not exist.
Use GET /api/v1/order for the customer's order list.

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
* Admin order detail
* Admin order status update
* Admin home order summary
* CUSTOMER access to /api/v1/admin/** is blocked

Valid status transition flow:

PAID -> PREPARING
PREPARING -> READY
READY -> PICKED_UP

When an order is changed to READY, a PICKUP_READY notification is created for the customer.

⸻

Notification

Completed:

* Notification persistence
* Customer notification list
* PICKUP_READY notification creation when an order becomes READY

FCM push notification has not been implemented yet.

The current notification feature is in-app notification through the notifications table.

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

Example:

0017-...
0018-...

Order data should be created through APIs whenever possible.

Reason:

Stock decrease
Price snapshot
Cart cleanup
Stock restore on cancellation
Notification creation when READY

These behaviors are implemented in service logic, so bypassing APIs may break consistency.

⸻

7. Remaining Candidate Features

1. End-to-End Demo Flow Check

This has the highest priority.

CUSTOMER checklist:

Login
Product list
NFC lookup
Product detail
Add to cart
Get cart
Create order
Order list
Order detail
Notification list
My Page

STAFF checklist:

Login
Admin home summary
Admin order list
Admin order detail
Update order status
Create notification when READY
Mark order as PICKED_UP

⸻

2. PATCH /api/v1/users/me

My Page profile update API.

Editable fields:

name
phone
height
weight
ageGroup
address

Do not update these fields through this API:

loginId
role
password
profileImageUrl
tryOnImageUrl

Images should be handled by separate multipart upload APIs later.

⸻

3. Image Upload APIs

The image upload flow still requires frontend discussion.

Candidate APIs:

PATCH /api/v1/users/me/profile-image
PATCH /api/v1/users/me/try-on-image

Candidate upload strategies:

Server-side multipart upload
Presigned URL upload

For now, customer04 already has a demo S3 URL, so the “registered image” state can be demonstrated.

⸻

4. AI Virtual Try-On

The try_ons table already exists.

Role separation:

users.try_on_image_url

The default full-body image registered by the user.

try_ons.original_image_url

Snapshot of the original image used for a specific try-on request.

try_ons.generated_image_url

AI-generated result image.

try_ons.status

Try-on processing status.

Actual AI integration has not been implemented yet.

Recommended approach:

Start with a mock generated image.
Then integrate an external AI service later.

Possible statuses:

PENDING
PROCESSING
DONE
FAILED

⸻

5. FCM Push Notification

Currently, notifications are only saved in the database.

FCM requires:

Android fcm_token registration
Firebase configuration
Push sending logic
Device permission handling

If the demo is close, this should be lower priority.

⸻

6. Swagger Error Documentation

Not all domains have full @ApiResponses documentation.

This improves documentation quality, but it is not critical for the demo flow.

⸻

8. Recommended Priority

If the demo is close, follow this order:

1. End-to-End demo flow check
2. Fix missing fields or broken API responses found during the E2E check
3. PATCH /api/v1/users/me
4. Check whether notification responses include orderId
5. Check whether admin order detail includes customer name and phone
6. Image upload APIs
7. AI virtual try-on mock
8. FCM push notification
9. Swagger error documentation

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

Use this path for the customer order list:

GET /api/v1/order

Do not use:

GET /api/v1/order/me

Use this path for the cart:

GET /api/v1/cart

Use this path for notifications:

GET /api/v1/notifications

Use this path for My Page summary:

GET /api/v1/users/me

Use these paths for admin orders:

GET /api/v1/admin/orders
GET /api/v1/admin/orders/{orderId}
PATCH /api/v1/admin/orders/{orderId}/status
GET /api/v1/admin/orders/summary

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
-> Customer notification
-> Pickup