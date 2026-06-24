#!/usr/bin/env python3
# ─────────────────────────────────────────────────────────────────────────────
#  ★ FitPick 시연용 임시 데이터 시드 스크립트 (시연 후 정리 예정) ★
# ─────────────────────────────────────────────────────────────────────────────
#  유니클로 한국 사이트 공개 상품 API를 호출해 옷/옵션/이미지를 자동 등록한다.
#  - robots.txt 확인: /kr/ko/products/* 허용
#  - 시연용 데모. 시연 종료 후 scripts/uniqlo_inserted_ids.txt 보고 정리.
#
#  실행 위치: EC2 (~/FitPick_BE)
#    python3 / requests / boto3 / docker / aws cli 전제 (EC2 글로벌에 설치됨)
#    .env에서 MYSQL_USER, MYSQL_PASSWORD, MYSQL_DATABASE 로딩
#    AWS 자격증명은 EC2 IAM Role 자동 인식
#
#  사용법:
#    python3 scripts/uniqlo_seed.py --dry-run                # 전체 dry run
#    python3 scripts/uniqlo_seed.py --dry-run E482502        # 1건 dry run
#    python3 scripts/uniqlo_seed.py                          # 전체 실행
#    python3 scripts/uniqlo_seed.py E482502                  # 1건만 실행 (테스트)
# ─────────────────────────────────────────────────────────────────────────────

from __future__ import annotations

import argparse
import datetime as dt
import json
import random
import re
import subprocess
import sys
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Optional

import boto3
import requests
from botocore.exceptions import ClientError


# ── 상수 ─────────────────────────────────────────────────────────────────────

SCRIPT_DIR = Path(__file__).resolve().parent
PROJECT_DIR = SCRIPT_DIR.parent
ENV_PATH = PROJECT_DIR / ".env"
INSERTED_IDS_PATH = SCRIPT_DIR / "uniqlo_inserted_ids.txt"

S3_BUCKET = "fitpick-images"
S3_REGION = "ap-northeast-2"
S3_PUBLIC_BASE = f"https://{S3_BUCKET}.s3.{S3_REGION}.amazonaws.com"
S3_KEY_PREFIX = "products"

DOCKER_MYSQL_CONTAINER = "fitpick-mysql"
DEFAULT_STORE_ID = 1
DEFAULT_STOCK = 10
DETAIL_IMAGE_COUNT = 3            # 썸네일 1 + 상세 3 = 총 4행 (명세 "3~4행" 충족)
RATE_LIMIT_SEC = 1.0
EXPAND_STOCK_ZERO_RATIO = 0.15    # expand-options: 15% 확률로 품절(0), 나머지 1~10

API_URL_FMT = (
    "https://www.uniqlo.com/kr/api/commerce/v5/ko/products/"
    "{product_id}/price-groups/00/details"
)
USER_AGENT = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
    "AppleWebKit/537.36 (KHTML, like Gecko) "
    "Chrome/131.0.0.0 Safari/537.36"
)
HTTP_HEADERS = {
    "User-Agent": USER_AGENT,
    "Accept": "application/json, text/plain, */*",
    "Accept-Language": "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7",
}


# ── 색상 매핑 (응답 영문 → 한국어, 기존 데이터와 톤 통일) ────────────────────

COLOR_KO: dict[str, str] = {
    "WHITE": "화이트", "BLACK": "블랙", "GRAY": "그레이",
    "NAVY": "네이비", "BLUE": "블루", "RED": "레드",
    "PINK": "핑크", "YELLOW": "옐로우", "GREEN": "그린",
    "BEIGE": "베이지", "BROWN": "브라운", "KHAKI": "카키",
    "ORANGE": "오렌지", "PURPLE": "퍼플", "OLIVE": "올리브",
    "WINE": "와인", "OFF WHITE": "오프화이트",
}


# ── 시연 대상 상품 ───────────────────────────────────────────────────────────
# (productCode, [(colorDisplayCode, sizeDisplayCode), ...], ClothesCategory)

@dataclass(frozen=True)
class ProductSpec:
    code: str                              # 예: E482502 (API 호출 시 -000 자동 부착)
    options: list[tuple[str, str]]
    category: str                          # TOP/BOTTOM/OUTER/DRESS/SHOES/BAG/ACCESSORY


PRODUCTS: list[ProductSpec] = [
    ProductSpec("E482502", [("69", "005")],                "TOP"),     # 셔츠 (원래 26 단종 → 69 NAVY로 대체)
    ProductSpec("E489137", [("13", "005")],                "TOP"),
    ProductSpec("E482480", [("69", "005")],                "TOP"),
    ProductSpec("E475367", [("18", "004")],                "TOP"),     # 티셔츠
    ProductSpec("E485567", [("01", "004")],                "TOP"),
    ProductSpec("E488088", [("00", "004")],                "TOP"),     # 원래 (01,004) 단종 → (00,004) 단일
    ProductSpec("E485711", [("69", "004")],                "TOP"),
    ProductSpec("E484209", [("66", "004")],                "BOTTOM"),  # 바지
    ProductSpec("E482883", [("35", "004")],                "BOTTOM"),
    ProductSpec("E489125", [("65", "004")],                "BOTTOM"),
    ProductSpec("E470542", [("66", "030")],                "BOTTOM"),
    ProductSpec("E488859", [("57", "999")],                "BAG"),     # 가방
    ProductSpec("E484085", [("65", "999")],                "BAG"),
    ProductSpec("E462191", [("01", "004")],                "BAG"),
    ProductSpec("E484086", [("74", "999")],                "BAG"),

    # 추가 시연 후보: 기존 seed와 중복 없는 OUTER/DRESS/SHOES/ACCESSORY 보강
    ProductSpec("E465203", [("57", "005")],                "OUTER"),     # DRY-EX UV PROTECTION풀집파카 / OLIVE / L
    ProductSpec("E469292", [("69", "005")],                "OUTER"),     # 포켓터블UV PROTECTION파카 / NAVY / L
    ProductSpec("E483986", [("31", "004")],                "DRESS"),     # AIRism코튼T원피스(반팔) / BEIGE / M
    ProductSpec("E487286", [("68", "004")],                "DRESS"),     # 콤비네이션티어드원피스(슬리브리스) / BLUE / M
    ProductSpec("E484330", [("32", "260")],                "SHOES"),     # 콤비네이션스니커즈 / BEIGE / 26
    ProductSpec("E482815", [("09", "240")],                "SHOES"),     # 스퀘어플랫슈즈 / BLACK / 24
    ProductSpec("E433776", [("69", "999")],                "ACCESSORY"), # UV PROTECTION컴팩트엄브렐라 / NAVY / FREE
    ProductSpec("E478307", [("31", "999")],                "ACCESSORY"), # UV PROTECTION캡(트윌) / BEIGE / FREE

    # 추가 시연 보강분 (사용자 지정 URL 기반)
    # E484610, E482982 는 material 텍스트가 컬럼 길이(VARCHAR(100))를 초과해 제외.
    ProductSpec("E468671", [("05", "005")],                "OUTER"),
    ProductSpec("E486167", [("55", "005")],                "OUTER"),
    ProductSpec("E482443", [("69", "005")],                "OUTER"),
    ProductSpec("E489142", [("62", "004")],                "BOTTOM"),
    ProductSpec("E488997", [("07", "004")],                "BOTTOM"),
    ProductSpec("E488371", [("57", "004")],                "BOTTOM"),
    ProductSpec("E484026", [("01", "004")],                "DRESS"),
]


# ── 파싱 결과 모델 ──────────────────────────────────────────────────────────

@dataclass
class OptionData:
    color_dc: str
    color_ko: str            # 저장값 (한국어 또는 매핑 누락 시 영문)
    color_en: str            # 응답 원본 영문 (로그용)
    color_fallback: bool     # True면 매핑 누락 → 영문 그대로 저장
    size_dc: str
    size_name: str           # 저장값 ("L", "FREE", "30" 등 사람 읽을 수 있는 값)


@dataclass
class ParsedProduct:
    code: str
    title: str
    description: str
    material: str
    price: int
    category: str
    breadcrumb: str
    first_color_dc: str
    thumbnail_url_src: str
    detail_urls_src: list[str]
    options: list[OptionData]


# ── 유틸 ─────────────────────────────────────────────────────────────────────

def die(msg: str, code: int = 1) -> None:
    print(f"[FATAL] {msg}", file=sys.stderr)
    sys.exit(code)


def log(msg: str) -> None:
    print(msg, flush=True)


def load_env(env_path: Path) -> dict[str, str]:
    """간단한 KEY=VALUE 파싱 (quote 제거, 주석/빈 줄 무시)."""
    out: dict[str, str] = {}
    for raw in env_path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, v = line.split("=", 1)
        out[k.strip()] = v.strip().strip('"').strip("'")
    return out


def sql_quote(s: Optional[str]) -> str:
    """MySQL string literal escape. None → NULL."""
    if s is None:
        return "NULL"
    s = s.replace("\\", "\\\\").replace("'", "\\'").replace("\x00", "")
    return "'" + s + "'"


def normalize_description(long_desc: str, design_detail: str) -> str:
    parts = [p for p in (long_desc, design_detail) if p]
    text = "\n\n".join(parts)
    text = re.sub(r"<br\s*/?>", "\n", text, flags=re.IGNORECASE)
    text = re.sub(r"[ \t]+\n", "\n", text)
    text = re.sub(r"\n{3,}", "\n\n", text)
    return text.strip()


def map_color(en: str) -> tuple[str, str, bool]:
    """영문 색상명 → (저장값, 원본 영문 대문자, fallback여부)."""
    e = (en or "").upper().strip()
    if e in COLOR_KO:
        return COLOR_KO[e], e, False
    return e, e, True


# ── 유니클로 API ─────────────────────────────────────────────────────────────

def fetch_product(product_code: str) -> dict:
    product_id = f"{product_code}-000"
    url = API_URL_FMT.format(product_id=product_id)
    headers = dict(HTTP_HEADERS)
    headers["Referer"] = f"https://www.uniqlo.com/kr/ko/products/{product_id}/00"
    resp = requests.get(url, headers=headers, timeout=15)
    resp.raise_for_status()
    data = resp.json()
    if data.get("status") != "ok":
        raise RuntimeError(f"API status nok: {json.dumps(data)[:300]}")
    return data["result"]


def parse_product(raw: dict, spec: ProductSpec) -> ParsedProduct:
    title = raw["name"]
    description = normalize_description(
        raw.get("longDescription", "") or "",
        raw.get("designDetail", "") or "",
    )
    material = raw.get("composition", "") or ""
    price = int(raw["prices"]["base"]["value"])
    breadcrumb = " / ".join(
        raw.get("breadcrumbs", {}).get(k, {}).get("locale", "")
        for k in ("gender", "class", "category", "subcategory")
    )

    color_by_dc: dict[str, str] = {
        c["displayCode"]: c["name"] for c in raw.get("colors", [])
    }
    size_by_dc: dict[str, str] = {
        s["displayCode"]: s["name"] for s in raw.get("sizes", [])
    }

    options: list[OptionData] = []
    for color_dc, size_dc in spec.options:
        en = color_by_dc.get(color_dc)
        if en is None:
            raise RuntimeError(
                f"colorDisplayCode={color_dc} 응답에 없음. "
                f"available={list(color_by_dc)}"
            )
        size_name = size_by_dc.get(size_dc)
        if size_name is None:
            raise RuntimeError(
                f"sizeDisplayCode={size_dc} 응답에 없음. "
                f"available={list(size_by_dc)}"
            )
        ko, en_norm, fallback = map_color(en)
        options.append(OptionData(
            color_dc=color_dc, color_ko=ko, color_en=en_norm,
            color_fallback=fallback,
            size_dc=size_dc, size_name=size_name,
        ))

    first_color_dc = spec.options[0][0]
    images = raw.get("images", {})
    main_entry = images.get("main", {}).get(first_color_dc, {})
    thumbnail_src = main_entry.get("image")
    if not thumbnail_src:
        raise RuntimeError(f"main image not found for color={first_color_dc}")

    detail_srcs: list[str] = []
    for s in images.get("sub", []):
        if "image" in s:                    # video 키는 skip
            detail_srcs.append(s["image"])
        if len(detail_srcs) >= DETAIL_IMAGE_COUNT:
            break
    if len(detail_srcs) < DETAIL_IMAGE_COUNT:
        for f in images.get("features", []):
            url = f.get("imageUrl")
            if url:
                detail_srcs.append(url)
            if len(detail_srcs) >= DETAIL_IMAGE_COUNT:
                break

    return ParsedProduct(
        code=spec.code, title=title, description=description,
        material=material, price=price, category=spec.category,
        breadcrumb=breadcrumb, first_color_dc=first_color_dc,
        thumbnail_url_src=thumbnail_src, detail_urls_src=detail_srcs,
        options=options,
    )


# ── S3 ──────────────────────────────────────────────────────────────────────

def s3_client():
    return boto3.client("s3", region_name=S3_REGION)


def s3_exists(client, key: str) -> bool:
    try:
        client.head_object(Bucket=S3_BUCKET, Key=key)
        return True
    except ClientError as e:
        code = e.response.get("Error", {}).get("Code", "")
        if code in ("404", "NoSuchKey", "NotFound"):
            return False
        raise


def s3_upload_from_url(client, src_url: str, key: str) -> tuple[str, bool]:
    """이미 있으면 (url, True[=skip]). 없으면 다운로드 후 업로드 (url, False)."""
    public_url = f"{S3_PUBLIC_BASE}/{key}"
    if s3_exists(client, key):
        return public_url, True
    headers = {"User-Agent": USER_AGENT, "Referer": "https://www.uniqlo.com/"}
    r = requests.get(src_url, headers=headers, timeout=20)
    r.raise_for_status()
    content_type = r.headers.get("Content-Type", "image/jpeg").split(";")[0].strip()
    # 버킷이 "Bucket owner enforced" (ACL 비활성)이므로 ACL 인자 안 전달.
    # 공개 접근은 버킷 정책으로 보장됨 (기존 demo 이미지와 동일 경로 사용).
    client.put_object(
        Bucket=S3_BUCKET, Key=key, Body=r.content,
        ContentType=content_type,
    )
    return public_url, False


def s3_key_for(product_code: str, role: str, ext: str) -> str:
    return f"{S3_KEY_PREFIX}/{product_code}/{role}.{ext}"


def ext_from_url(url: str) -> str:
    m = re.search(r"\.([A-Za-z0-9]+)(?:\?|$)", url)
    return (m.group(1) if m else "jpg").lower()


# ── DB (docker exec mysql) ───────────────────────────────────────────────────

def docker_mysql_run(env: dict[str, str], sql: str) -> tuple[int, str, str]:
    cmd = [
        "docker", "exec", "-i", DOCKER_MYSQL_CONTAINER,
        "mysql",
        "--default-character-set=utf8mb4",   # stdin UTF-8 바이트 보존 (mojibake 방지)
        "-u", env["MYSQL_USER"],
        "-p" + env["MYSQL_PASSWORD"],
        env["MYSQL_DATABASE"],
    ]
    proc = subprocess.run(
        cmd, input=sql, capture_output=True, text=True, encoding="utf-8",
    )
    return proc.returncode, proc.stdout, proc.stderr


def insert_product_tx(
    parsed: ParsedProduct,
    thumb_url: str,
    detail_urls: list[str],
    env: dict[str, str],
) -> int:
    """한 productCode를 트랜잭션으로 INSERT. 실패 시 ROLLBACK. Returns: 새 clothes.id."""
    stmts: list[str] = []
    stmts.append(
        "INSERT INTO clothes "
        "(store_id, title, description, category, material, price, thumbnail_image_url, is_active) "
        f"VALUES ({DEFAULT_STORE_ID}, {sql_quote(parsed.title)}, "
        f"{sql_quote(parsed.description) if parsed.description else 'NULL'}, "
        f"{sql_quote(parsed.category)}, "
        f"{sql_quote(parsed.material) if parsed.material else 'NULL'}, "
        f"{parsed.price}, {sql_quote(thumb_url)}, true);"
    )
    stmts.append("SET @cid := LAST_INSERT_ID();")
    for opt in parsed.options:
        stmts.append(
            "INSERT INTO clothes_options (clothes_id, size, color, stock_quantity) "
            f"VALUES (@cid, {sql_quote(opt.size_name)}, {sql_quote(opt.color_ko)}, {DEFAULT_STOCK});"
        )
    # 썸네일도 clothes_images에 sort_order=0으로 같이 등록 (기존 컨벤션과 동일)
    stmts.append(
        "INSERT INTO clothes_images (clothes_id, image_url, sort_order) "
        f"VALUES (@cid, {sql_quote(thumb_url)}, 0);"
    )
    for i, u in enumerate(detail_urls, start=1):
        stmts.append(
            "INSERT INTO clothes_images (clothes_id, image_url, sort_order) "
            f"VALUES (@cid, {sql_quote(u)}, {i});"
        )
    stmts.append("SELECT @cid AS new_clothes_id;")

    sql = "START TRANSACTION;\n" + "\n".join(stmts) + "\nCOMMIT;\n"
    rc, out, err = docker_mysql_run(env, sql)
    if rc != 0:
        raise RuntimeError(f"mysql exec rc={rc}: {err.strip()}\nSQL:\n{sql}")

    cid: Optional[int] = None
    for ln in reversed(out.splitlines()):
        s = ln.strip()
        if s.isdigit():
            cid = int(s)
            break
    if cid is None:
        raise RuntimeError(f"new_clothes_id 파싱 실패. stdout={out!r}")
    return cid


# ── inserted_ids.txt ─────────────────────────────────────────────────────────

_INSERTED_HEADER = """# 시연용 임시 데이터 (scripts/uniqlo_seed.py로 등록).
# 시연 종료 후 정리 (EC2 ~/FitPick_BE 에서 실행):
#   set -a; source .env; set +a
#   ids=$(awk 'NF && $1 !~ /^#/ {print $2}' scripts/uniqlo_inserted_ids.txt | paste -sd,)
#   docker exec -i fitpick-mysql mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" <<SQL
#   DELETE FROM clothes_images  WHERE clothes_id IN ($ids);
#   DELETE FROM clothes_options WHERE clothes_id IN ($ids);
#   DELETE FROM clothes         WHERE id        IN ($ids);
#   SQL
# 형식: <productCode> <clothes_id> <inserted_at_iso>
"""


def load_inserted_codes() -> set[str]:
    if not INSERTED_IDS_PATH.exists():
        return set()
    out: set[str] = set()
    for ln in INSERTED_IDS_PATH.read_text(encoding="utf-8").splitlines():
        ln = ln.strip()
        if not ln or ln.startswith("#"):
            continue
        parts = ln.split()
        if parts:
            out.add(parts[0])
    return out


def load_inserted_id_map() -> dict[str, int]:
    """productCode → clothes_id (expand-options 모드용)."""
    out: dict[str, int] = {}
    if not INSERTED_IDS_PATH.exists():
        return out
    for ln in INSERTED_IDS_PATH.read_text(encoding="utf-8").splitlines():
        ln = ln.strip()
        if not ln or ln.startswith("#"):
            continue
        parts = ln.split()
        if len(parts) >= 2 and parts[1].isdigit():
            out[parts[0]] = int(parts[1])
    return out


def append_inserted(product_code: str, clothes_id: int) -> None:
    if not INSERTED_IDS_PATH.exists():
        INSERTED_IDS_PATH.write_text(_INSERTED_HEADER, encoding="utf-8")
    ts = dt.datetime.now().isoformat(timespec="seconds")
    with INSERTED_IDS_PATH.open("a", encoding="utf-8") as f:
        f.write(f"{product_code} {clothes_id} {ts}\n")


# ── expand-options 모드 ─────────────────────────────────────────────────────

def random_stock() -> int:
    """15% 품절(0), 나머지는 1~10 균등."""
    if random.random() < EXPAND_STOCK_ZERO_RATIO:
        return 0
    return random.randint(1, 10)


def fetch_existing_options(cid: int, env: dict[str, str]) -> set[tuple[str, str]]:
    """clothes_id의 (size, color) 조합 set 반환. 중복 INSERT 회피용."""
    sql = (
        f"SELECT IFNULL(size,''), IFNULL(color,'') "
        f"FROM clothes_options WHERE clothes_id = {cid};"
    )
    rc, out, err = docker_mysql_run(env, sql)
    if rc != 0:
        raise RuntimeError(f"기존 옵션 조회 실패 (cid={cid}): {err.strip()}")
    existing: set[tuple[str, str]] = set()
    lines = [ln for ln in out.splitlines() if ln.strip()]
    # 첫 줄은 헤더 — batch 모드 stdin pipe 출력
    for ln in lines[1:]:
        parts = ln.split("\t")
        if len(parts) >= 2:
            existing.add((parts[0], parts[1]))
    return existing


def insert_options_batch(
    cid: int,
    rows: list[tuple[str, str, int]],
    env: dict[str, str],
) -> None:
    """옵션 여러 개를 한 트랜잭션으로 INSERT."""
    if not rows:
        return
    stmts = ["START TRANSACTION;"]
    for size_name, color_value, stock in rows:
        stmts.append(
            "INSERT INTO clothes_options (clothes_id, size, color, stock_quantity) "
            f"VALUES ({cid}, {sql_quote(size_name)}, {sql_quote(color_value)}, {stock});"
        )
    stmts.append("COMMIT;")
    sql = "\n".join(stmts) + "\n"
    rc, out, err = docker_mysql_run(env, sql)
    if rc != 0:
        raise RuntimeError(f"옵션 batch INSERT 실패 (cid={cid}): {err.strip()}")


def run_expand_options(specs: list[ProductSpec], env: dict[str, str]) -> int:
    """등록된 옷에 응답의 모든 (color × size) 조합을 옵션으로 추가. 중복 skip."""
    code_to_cid = load_inserted_id_map()
    if not code_to_cid:
        die(f"{INSERTED_IDS_PATH} 없음/비어있음. 먼저 정상 시드 실행 필요.")

    total_new = total_skip = 0
    fallback_colors: set[str] = set()
    failures: list[tuple[str, str]] = []

    for i, spec in enumerate(specs, start=1):
        cid = code_to_cid.get(spec.code)
        log(f"\n[{i}/{len(specs)}] {spec.code}  clothes_id={cid}")
        if cid is None:
            log("  ⚠ inserted_ids.txt에 없음 → skip")
            continue
        try:
            raw = fetch_product(spec.code)
            all_colors = raw.get("colors", [])
            all_sizes = raw.get("sizes", [])
            existing = fetch_existing_options(cid, env)

            new_rows: list[tuple[str, str, int]] = []
            color_summary: list[str] = []
            for c in all_colors:
                en = c.get("name") or ""
                color_ko, en_norm, fallback = map_color(en)
                if fallback:
                    fallback_colors.add(en_norm)
                color_summary.append(f"{color_ko}{'*' if fallback else ''}")
                for s in all_sizes:
                    size_name = s.get("name") or ""
                    if not size_name:
                        continue
                    key = (size_name, color_ko)
                    if key in existing:
                        continue
                    new_rows.append((size_name, color_ko, random_stock()))

            insert_options_batch(cid, new_rows, env)
            log(f"  colors: {len(all_colors)} [{', '.join(color_summary)}]  "
                f"(* = 영문 fallback)")
            log(f"  sizes:  {len(all_sizes)} [{', '.join(s.get('name','') for s in all_sizes)}]")
            log(f"  옵션: 신규 +{len(new_rows)}, skip {len(existing)} (기존)")
            total_new += len(new_rows)
            total_skip += len(existing)
        except Exception as e:
            log(f"  ✗ FAIL: {e}")
            failures.append((spec.code, str(e)))
        time.sleep(RATE_LIMIT_SEC)

    log("\n" + "=" * 60)
    log(f"  신규 옵션: {total_new}, 기존 skip: {total_skip}, 실패: {len(failures)}")
    if fallback_colors:
        log(f"  영문 fallback 색상: {sorted(fallback_colors)}")
    if failures:
        log("  실패 상세:")
        for code, msg in failures:
            log(f"    - {code}: {msg}")
    log("=" * 60)
    return 0 if not failures else 2


# ── 메인 흐름 ────────────────────────────────────────────────────────────────

def run_dry_run(specs: list[ProductSpec]) -> int:
    for i, spec in enumerate(specs, start=1):
        log(f"\n[{i}/{len(specs)}] {spec.code}  category={spec.category}")
        try:
            raw = fetch_product(spec.code)
            parsed = parse_product(raw, spec)
        except Exception as e:
            log(f"  ! FAIL: {e}")
            continue
        log(f"  title:       {parsed.title}")
        log(f"  price:       {parsed.price:,} KRW")
        log(f"  breadcrumb:  {parsed.breadcrumb}")
        log(f"  material:    {parsed.material[:80]}")
        desc_preview = (parsed.description[:80] + "…") if len(parsed.description) > 80 else parsed.description
        log(f"  description: {desc_preview!r}")
        log(f"  thumbnail:   {parsed.thumbnail_url_src}")
        for j, u in enumerate(parsed.detail_urls_src, start=1):
            log(f"  detail-{j}:    {u}")
        for opt in parsed.options:
            tag = "  [FALLBACK]" if opt.color_fallback else ""
            log(f"  option:      color={opt.color_ko} ({opt.color_en}) / size={opt.size_name}{tag}")
        time.sleep(RATE_LIMIT_SEC)
    log("\n[DRY RUN] 완료. S3/DB 변경 없음.")
    return 0


def run_full(specs: list[ProductSpec], env: dict[str, str]) -> int:
    already = load_inserted_codes()
    s3 = s3_client()

    n_ok = n_skip = n_fail = 0
    new_ids: list[int] = []
    failures: list[tuple[str, str]] = []

    for i, spec in enumerate(specs, start=1):
        log(f"\n[{i}/{len(specs)}] {spec.code}  category={spec.category}")
        if spec.code in already:
            log("  ⚠ already in inserted_ids.txt → skip")
            n_skip += 1
            continue
        try:
            raw = fetch_product(spec.code)
            parsed = parse_product(raw, spec)
            log(f"  parsed: {parsed.title} / {parsed.price:,} KRW / opts={len(parsed.options)}")
            for opt in parsed.options:
                tag = "  [FALLBACK]" if opt.color_fallback else ""
                log(f"    - {opt.color_ko} ({opt.color_en}) / {opt.size_name}{tag}")

            thumb_key = s3_key_for(spec.code, "thumbnail", ext_from_url(parsed.thumbnail_url_src))
            thumb_url, skipped = s3_upload_from_url(s3, parsed.thumbnail_url_src, thumb_key)
            log(f"  s3 thumb     [{'skip' if skipped else 'upload'}] {thumb_url}")

            detail_urls: list[str] = []
            for j, src in enumerate(parsed.detail_urls_src, start=1):
                key = s3_key_for(spec.code, f"detail-{j}", ext_from_url(src))
                url, skipped = s3_upload_from_url(s3, src, key)
                detail_urls.append(url)
                log(f"  s3 detail-{j} [{'skip' if skipped else 'upload'}] {url}")

            cid = insert_product_tx(parsed, thumb_url, detail_urls, env)
            log(f"  ✓ INSERT  clothes_id={cid}")
            append_inserted(spec.code, cid)
            new_ids.append(cid)
            n_ok += 1
        except Exception as e:
            log(f"  ✗ FAIL: {e}")
            failures.append((spec.code, str(e)))
            n_fail += 1
        time.sleep(RATE_LIMIT_SEC)

    log("\n" + "=" * 60)
    log(f"  성공: {n_ok}, skip: {n_skip}, 실패: {n_fail}")
    if new_ids:
        log(f"  새 clothes_id: {new_ids}")
    if failures:
        log("  실패 상세:")
        for code, msg in failures:
            log(f"    - {code}: {msg}")
    log(f"  ID 로그: {INSERTED_IDS_PATH}")
    log("=" * 60)
    return 0 if n_fail == 0 else 2


def main() -> int:
    parser = argparse.ArgumentParser(
        description="FitPick 시연용 유니클로 상품 시드 (API → S3 → DB 트랜잭션). 시연 후 정리 예정."
    )
    parser.add_argument("--dry-run", action="store_true",
                        help="API만 호출. S3/DB 건드리지 않음.")
    parser.add_argument("--expand-options", action="store_true",
                        help="등록된 옷에 응답의 모든 color×size 조합을 옵션으로 추가 (중복 skip, idempotent)")
    parser.add_argument("only_codes", nargs="*",
                        help="특정 productCode만 처리 (예: E482502 E489137)")
    args = parser.parse_args()

    if args.only_codes:
        only = set(args.only_codes)
        specs = [p for p in PRODUCTS if p.code in only]
        missing = only - {p.code for p in PRODUCTS}
        if missing:
            die(f"PRODUCTS에 없는 코드: {sorted(missing)}")
    else:
        specs = list(PRODUCTS)

    if args.dry_run:
        return run_dry_run(specs)

    if not ENV_PATH.exists():
        die(f".env 없음: {ENV_PATH}. EC2의 ~/FitPick_BE에서 실행 필요.")
    env = load_env(ENV_PATH)
    for k in ("MYSQL_USER", "MYSQL_PASSWORD", "MYSQL_DATABASE"):
        if not env.get(k):
            die(f".env에 {k} 없음")

    if args.expand_options:
        return run_expand_options(specs, env)
    return run_full(specs, env)


if __name__ == "__main__":
    sys.exit(main())
