package com.fitpick.domain.nfc.controller.docs;

import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "NFC", description = "NFC 태깅 상품 조회 API")
public interface NfcApiDocs {

    @Operation(
            summary = "NFC 태깅 상품 조회 (옷+옵션 단위)",
            description = "NFC 태그 tagUid로 연결된 옷/옵션 식별 정보를 조회합니다. " +
                    "옵션 단위 태그면 해당 옵션 정보를, 옷 단위 태그(레거시)면 첫 번째 옵션으로 폴백합니다. " +
                    "응답은 식별 정보(clothesId, clothesOptionId, title, size, color)만 반환하며, " +
                    "상세 정보(이미지/재고/가격/다른 옵션)는 GET /api/v1/clothes/{id}에서 받습니다. " +
                    "JWT 없이 호출 가능하며, 로그인 사용자는 조회 이력(NFC_TAG)이 기록됩니다."
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 — NfcTagResponse 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "등록되지 않았거나 비활성화된 NFC 태그 (N001) 또는 상품 없음/비활성 (C001)")
    })
    ApiResponse<?> getClothesByTagUid(
            @Parameter(description = "NFC 태그 고유 식별자 (tagUid)", example = "04:AB:CD:EF:12:34", required = true)
            String tagUid,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );
}
