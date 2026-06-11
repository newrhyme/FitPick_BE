#!/bin/bash
# ═══════════════════════════════════════════════════════════════
#  FitPick 시연용 더미 데이터 세팅 스크립트
#  실행 위치: EC2 서버 (~/FitPick_BE/scripts/seed.sh)
#  전제 조건: jq, docker, ~/FitPick_BE/.env 존재
#  ※ 실행 전 EC2에서 정리 SQL 먼저 돌릴 것:
#     DELETE FROM order_items; DELETE FROM orders;
#     DELETE FROM cart_items;  DELETE FROM carts;
# ═══════════════════════════════════════════════════════════════
set -uo pipefail

BASE_URL="http://3.39.34.202:8080"
PW="pass01"  # 모든 계정 공용 비밀번호 (GPT 노트 기준)

# ── .env 소싱 (DB 자격증명) ──
source ~/FitPick_BE/.env
DB="${MYSQL_DATABASE:-fitpick}"

# ─────────────────────────────────────────────────────────────
# 헬퍼
# ─────────────────────────────────────────────────────────────
db() {
    docker exec fitpick-mysql mysql \
        -u"${MYSQL_USER}" -p"${MYSQL_PASSWORD}" "$DB" \
        --silent --skip-column-names -e "$1" 2>/dev/null
}

signup() {
    local loginId="$1" password="$2" name="$3"
    local http_code
    http_code=$(curl -s -o /dev/null -w "%{http_code}" \
        -X POST "$BASE_URL/api/v1/auth/signup" \
        -H "Content-Type: application/json" \
        -d "{\"loginId\":\"$loginId\",\"password\":\"$password\",\"name\":\"$name\"}")
    case "$http_code" in
        200|201) echo "  ✅ 회원가입: $loginId" ;;
        400|409) echo "  ⚠️  이미 존재 (스킵): $loginId" ;;
        *)       echo "  ❌ 회원가입 실패: $loginId (HTTP $http_code)" ;;
    esac
}

login() {
    local loginId="$1" password="$2"
    local token
    token=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"loginId\":\"$loginId\",\"password\":\"$password\"}" \
        | jq -r '.data.accessToken // empty')
    if [[ -z "$token" ]]; then
        echo "LOGIN_FAILED_${loginId}" >&2
        echo ""
    else
        echo "$token"
    fi
}

check_token() { [[ -n "$1" && "$1" != LOGIN_FAILED* ]] && echo "✅" || echo "❌ 실패"; }

# 상태 변경 (admin)
change_status() {
    local orderId="$1" status="$2" token="$3"
    curl -s -X PATCH "$BASE_URL/api/v1/admin/orders/$orderId/status" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $token" \
        -d "{\"status\":\"$status\"}" > /dev/null
}

echo ""
echo "══════════════════════════════════════════════════════"
echo "  FitPick 더미 데이터 세팅 시작"
echo "══════════════════════════════════════════════════════"

# ─────────────────────────────────────────────────────────────
echo ""
echo "── 1. 회원가입 (전부 pass01) ───────────────────────"
signup "customer01" "$PW" "김지민"
signup "customer02" "$PW" "이도윤"
signup "customer03" "$PW" "박서연"
signup "customer04" "$PW" "최현우"
signup "customer05" "$PW" "정하은"
signup "staff02"    "$PW" "매장직원02"
signup "staff03"    "$PW" "매장관리자01"

# ─────────────────────────────────────────────────────────────
echo ""
echo "── 2. staff02 / staff03 권한 → STAFF ───────────────"
db "UPDATE users SET role='STAFF' WHERE login_id IN ('staff02','staff03');"
echo "  ✅ 완료"

# ─────────────────────────────────────────────────────────────
echo ""
echo "── 3. 재고 초기화 (GPT 노트 기준 옵션 41~60) ───────"
db "
UPDATE clothes_options SET stock_quantity = 15 WHERE id = 41;
UPDATE clothes_options SET stock_quantity = 0  WHERE id = 42;
UPDATE clothes_options SET stock_quantity = 20 WHERE id = 43;
UPDATE clothes_options SET stock_quantity = 2  WHERE id = 44;
UPDATE clothes_options SET stock_quantity = 30 WHERE id = 45;
UPDATE clothes_options SET stock_quantity = 25 WHERE id = 46;
UPDATE clothes_options SET stock_quantity = 3  WHERE id = 47;
UPDATE clothes_options SET stock_quantity = 10 WHERE id = 48;
UPDATE clothes_options SET stock_quantity = 8  WHERE id = 49;
UPDATE clothes_options SET stock_quantity = 1  WHERE id = 50;
UPDATE clothes_options SET stock_quantity = 12 WHERE id = 51;
UPDATE clothes_options SET stock_quantity = 7  WHERE id = 52;
UPDATE clothes_options SET stock_quantity = 0  WHERE id = 53;
UPDATE clothes_options SET stock_quantity = 8  WHERE id = 54;
UPDATE clothes_options SET stock_quantity = 2  WHERE id = 55;
UPDATE clothes_options SET stock_quantity = 1  WHERE id = 56;
UPDATE clothes_options SET stock_quantity = 25 WHERE id = 57;
UPDATE clothes_options SET stock_quantity = 18 WHERE id = 58;
UPDATE clothes_options SET stock_quantity = 40 WHERE id = 59;
UPDATE clothes_options SET stock_quantity = 35 WHERE id = 60;
"
echo "  ✅ 옵션 20개 재고 초기화"

# ─────────────────────────────────────────────────────────────
echo ""
echo "── 4. 로그인 ────────────────────────────────────────"
TOKEN_STAFF01=$(login "staff01" "$PW")
TOKEN_C01=$(login "customer01" "$PW")
TOKEN_C02=$(login "customer02" "$PW")
TOKEN_C03=$(login "customer03" "$PW")
TOKEN_C04=$(login "customer04" "$PW")
TOKEN_C05=$(login "customer05" "$PW")
echo "  staff01:    $(check_token "$TOKEN_STAFF01")"
echo "  customer01: $(check_token "$TOKEN_C01")"
echo "  customer02: $(check_token "$TOKEN_C02")"
echo "  customer03: $(check_token "$TOKEN_C03")"
echo "  customer04: $(check_token "$TOKEN_C04")"
echo "  customer05: $(check_token "$TOKEN_C05")"

# ─────────────────────────────────────────────────────────────
echo ""
echo "── 5. 장바구니 테스트 데이터 (GPT 노트 7번) ────────"
# customer02: 옵션 2개
curl -s -X POST "$BASE_URL/api/v1/cart/items" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN_C02" \
    -d '{"optionId":43,"quantity":1}' > /dev/null  # 에어리즘 M/블랙
curl -s -X POST "$BASE_URL/api/v1/cart/items" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN_C02" \
    -d '{"optionId":46,"quantity":2}' > /dev/null  # 슈피마 M/화이트
echo "  ✅ customer02 장바구니: 옵션 43, 46"

# customer03: 옵션 1개
curl -s -X POST "$BASE_URL/api/v1/cart/items" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN_C03" \
    -d '{"optionId":48,"quantity":1}' > /dev/null  # 스키니진 30/인디고
echo "  ✅ customer03 장바구니: 옵션 48"

# ─────────────────────────────────────────────────────────────
echo ""
echo "── 6. 주문 시나리오 (GPT 노트 8번) ─────────────────"

# 6-1. PAID 주문: customer01 (direct)
ORDER_C01_PAID=$(curl -s -X POST "$BASE_URL/api/v1/order/direct" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN_C01" \
    -d '{"clothesOptionId":41,"quantity":1}' \
    | jq -r '.data.orderId')
echo "  ✅ PAID #1: customer01 orderId=$ORDER_C01_PAID (옵션 41)"

# 6-2. PAID 주문: customer02 (cart - 위에서 담은 거)
ORDER_C02_PAID=$(curl -s -X POST "$BASE_URL/api/v1/order/cart" \
    -H "Authorization: Bearer $TOKEN_C02" \
    | jq -r '.data.orderId')
echo "  ✅ PAID #2: customer02 orderId=$ORDER_C02_PAID (장바구니)"

# 6-3. PREPARING 주문: customer03 (cart - 위에서 담은 거) → PREPARING
ORDER_C03=$(curl -s -X POST "$BASE_URL/api/v1/order/cart" \
    -H "Authorization: Bearer $TOKEN_C03" \
    | jq -r '.data.orderId')
change_status "$ORDER_C03" "PREPARING" "$TOKEN_STAFF01"
echo "  ✅ PREPARING: customer03 orderId=$ORDER_C03"

# 6-4. READY 주문: customer04 (direct) → PREPARING → READY (알림)
ORDER_C04=$(curl -s -X POST "$BASE_URL/api/v1/order/direct" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN_C04" \
    -d '{"clothesOptionId":51,"quantity":1}' \
    | jq -r '.data.orderId')
change_status "$ORDER_C04" "PREPARING" "$TOKEN_STAFF01"
change_status "$ORDER_C04" "READY" "$TOKEN_STAFF01"
echo "  ✅ READY: customer04 orderId=$ORDER_C04 (옵션 51) — 알림 생성"

# 6-5. PICKED_UP 주문: customer05 (direct) → PREPARING → READY → PICKED_UP
ORDER_C05=$(curl -s -X POST "$BASE_URL/api/v1/order/direct" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN_C05" \
    -d '{"clothesOptionId":57,"quantity":1}' \
    | jq -r '.data.orderId')
change_status "$ORDER_C05" "PREPARING" "$TOKEN_STAFF01"
change_status "$ORDER_C05" "READY" "$TOKEN_STAFF01"
change_status "$ORDER_C05" "PICKED_UP" "$TOKEN_STAFF01"
echo "  ✅ PICKED_UP: customer05 orderId=$ORDER_C05 (옵션 57)"

# 6-6. CANCELED 주문: customer01 추가 주문 → 취소
ORDER_C01_CANCEL=$(curl -s -X POST "$BASE_URL/api/v1/order/direct" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN_C01" \
    -d '{"clothesOptionId":59,"quantity":1}' \
    | jq -r '.data.orderId')
curl -s -X POST "$BASE_URL/api/v1/order/$ORDER_C01_CANCEL/cancel" \
    -H "Authorization: Bearer $TOKEN_C01" > /dev/null
echo "  ✅ CANCELED: customer01 orderId=$ORDER_C01_CANCEL (옵션 59 → 재고 복구)"

# ─────────────────────────────────────────────────────────────
echo ""
echo "── 7. 검증: customer04 알림 (PICKUP_READY) ─────────"
NOTIF_RESP=$(curl -s "$BASE_URL/api/v1/notifications" \
    -H "Authorization: Bearer $TOKEN_C04")
echo "$NOTIF_RESP" | jq -r '.data.list[] | "  → [\(.notificationType)] \(.title) | 읽음:\(.isRead)"' 2>/dev/null || true

# ─────────────────────────────────────────────────────────────
echo ""
echo "── 8. 검증: 관리자 주문 요약 (PAID 2 / PREPARING 1 / READY 1 / PICKED_UP 1 / CANCELED 1 예상) ──"
STATS=$(curl -s "$BASE_URL/api/v1/admin/orders/summary" \
    -H "Authorization: Bearer $TOKEN_STAFF01")
echo "$STATS" | jq '.data'

# ─────────────────────────────────────────────────────────────
echo ""
echo "── 9. 검증: 권한/인증 차단 ─────────────────────────"
CODE_403=$(curl -s -o /dev/null -w "%{http_code}" \
    "$BASE_URL/api/v1/admin/orders" \
    -H "Authorization: Bearer $TOKEN_C01")
echo "  CUSTOMER → /admin/orders: HTTP $CODE_403 $([ "$CODE_403" = "403" ] && echo "✅" || echo "❌")"

CODE_401=$(curl -s -o /dev/null -w "%{http_code}" \
    "$BASE_URL/api/v1/cart")
echo "  토큰 없음 → /cart:        HTTP $CODE_401 $([ "$CODE_401" = "401" ] && echo "✅" || echo "❌")"

# ─────────────────────────────────────────────────────────────
echo ""
echo "══════════════════════════════════════════════════════"
echo "  최종 산출물 요약"
echo "══════════════════════════════════════════════════════"
echo ""
echo "  [계정] 전부 비밀번호 pass01"
echo "  staff01/02/03 (STAFF), customer01~05 (CUSTOMER)"
echo ""
echo "  [주문]"
echo "  PAID:      customer01 (#$ORDER_C01_PAID), customer02 (#$ORDER_C02_PAID)"
echo "  PREPARING: customer03 (#$ORDER_C03)"
echo "  READY:     customer04 (#$ORDER_C04)"
echo "  PICKED_UP: customer05 (#$ORDER_C05)"
echo "  CANCELED:  customer01 (#$ORDER_C01_CANCEL)"
echo ""
echo "  [Swagger] $BASE_URL/swagger-ui/index.html"
echo "  [NFC] 04A1B2C301~305, 04A1B2C309~310"
echo ""
echo "══════════════════════════════════════════════════════"
echo "  세팅 완료"
echo "══════════════════════════════════════════════════════"