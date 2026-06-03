package com.fitpick.domain.nfc.controller.docs;

import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "NFC", description = "NFC 태깅 상품 조회 API")
public interface NfcApiDocs {

    @Operation(
            summary = "NFC 태깅 상품 조회",
            description = "NFC 태그의 tagUid로 연결된 상품 상세를 조회합니다. 비로그인도 조회 가능하며, 로그인 사용자는 조회 이력(NFC_TAG)이 적재됩니다."
    )
    ApiResponse<?> getClothesByTagUid(String tagUid, @AuthenticationPrincipal CustomUserDetails userDetails);
}
