package com.fitpick.domain.tryon.controller.docs;

import com.fitpick.domain.tryon.dto.TryOnCreateRequest;
import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Try-On", description = "AI 가상 착용 API (OpenAI gpt-image-1 연동)")
public interface TryOnApiDocs {

    @Operation(
            summary = "가상 착용 이미지 생성",
            description = "사용자 전신 사진(users.try_on_image_url)과 선택한 상품 이미지를 OpenAI gpt-image-1에 보내 " +
                          "가상 착용 이미지를 생성하고 S3에 저장합니다. 준동기 처리(평균 20~40초 소요). " +
                          "전신 사진이 없으면 T001, 상품 이미지가 없으면 T002, 옵션 불일치 시 T006."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공 — TryOnResponse 반환 (status=DONE)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "전신 사진 미등록 (T001), 상품 이미지 없음 (T002), 옵션-상품 불일치 (T006), 입력값 검증 실패 (E001)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (E401)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품 없음 (C001) / 옵션 없음 (C002) / 사용자 없음 (A005)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "OpenAI 호출 또는 S3 업로드 실패 (T003) — try_ons.status=FAILED, failure_reason 기록")
    })
    ResponseEntity<ApiResponse<?>> create(CustomUserDetails userDetails, TryOnCreateRequest request);

    @Operation(
            summary = "가상 착용 단건 조회",
            description = "본인이 생성한 가상 착용 기록만 조회 가능. 타인의 tryOnId 요청 시 T005."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 — TryOnResponse"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (E401)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 기록 아님 (T005)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "기록 없음 (T004)")
    })
    ApiResponse<?> get(CustomUserDetails userDetails, Long tryOnId);

    @Operation(
            summary = "내 가상 착용 목록 조회",
            description = "본인이 생성한 가상 착용 기록 목록을 최신순으로 반환. " +
                          "각 항목에 상품명/옵션ID 포함 (N+1 없이 한 번에 fetch)."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 — List<TryOnListItemResponse>"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (E401)")
    })
    ApiResponse<?> getMyList(CustomUserDetails userDetails);
}
