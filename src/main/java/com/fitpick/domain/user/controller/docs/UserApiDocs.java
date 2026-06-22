package com.fitpick.domain.user.controller.docs;

import com.fitpick.domain.user.dto.FcmTokenUpdateRequest;
import com.fitpick.domain.user.dto.UserUpdateRequest;
import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "사용자 정보 API")
public interface UserApiDocs {

    @Operation(
            summary = "마이페이지 정보 조회",
            description = "로그인한 사용자의 마이페이지 정보를 조회합니다. " +
                          "주문 건수, 읽지 않은 알림 건수, 가상 착용 이미지 여부를 포함합니다. " +
                          "CUSTOMER/STAFF 모두 호출 가능합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 — UserMeResponse 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (E401)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음 (A005)")
    })
    ApiResponse<?> getMyInfo(CustomUserDetails userDetails);

    @Operation(
            summary = "내 정보 수정",
            description = "phone/height/weight/ageGroup/address 부분 수정. " +
                          "null 또는 누락된 필드는 변경하지 않습니다. " +
                          "loginId/role/password/profileImageUrl/tryOnImageUrl은 이 API로 수정할 수 없습니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공 — 변경된 UserMeResponse 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 본문 파싱 실패 또는 잘못된 ageGroup 값 (E000)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (E401)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음 (A005)")
    })
    ApiResponse<?> updateMyInfo(CustomUserDetails userDetails, UserUpdateRequest request);

    @Operation(
            summary = "프로필 이미지 업로드",
            description = "마이페이지 프로필 이미지를 S3에 업로드하고 users.profile_image_url을 갱신합니다. " +
                          "허용 형식: image/jpeg, image/png, image/webp. 최대 10MB. " +
                          "응답은 변경된 UserMeResponse를 반환합니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "object", requiredProperties = {"file"}),
                            encoding = @Encoding(name = "file", contentType = "image/jpeg, image/png, image/webp")
                    )
            )
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "업로드 성공 — 변경된 UserMeResponse 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "지원하지 않는 형식 (I001), 빈 파일 (I002), 또는 잘못된 요청 (E000) — 10MB 초과 포함"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (E401)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음 (A005)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "S3 업로드 실패 (I003)")
    })
    ApiResponse<?> uploadProfileImage(
            CustomUserDetails userDetails,
            @Parameter(description = "업로드할 프로필 이미지 (jpeg/png/webp, 최대 10MB)") MultipartFile file
    );

    @Operation(
            summary = "가상 착용용 전신 이미지 업로드",
            description = "가상 피팅 기본 이미지를 S3에 업로드하고 users.try_on_image_url을 갱신합니다. " +
                          "허용 형식: image/jpeg, image/png, image/webp. 최대 10MB. " +
                          "응답의 hasTryOnImage가 true로 바뀝니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "object", requiredProperties = {"file"}),
                            encoding = @Encoding(name = "file", contentType = "image/jpeg, image/png, image/webp")
                    )
            )
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "업로드 성공 — 변경된 UserMeResponse 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "지원하지 않는 형식 (I001), 빈 파일 (I002), 또는 잘못된 요청 (E000) — 10MB 초과 포함"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (E401)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음 (A005)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "S3 업로드 실패 (I003)")
    })
    ApiResponse<?> uploadTryOnImage(
            CustomUserDetails userDetails,
            @Parameter(description = "업로드할 전신 이미지 (jpeg/png/webp, 최대 10MB)") MultipartFile file
    );

    @Operation(
            summary = "FCM 토큰 등록/갱신",
            description = "안드로이드 앱이 FCM SDK로 발급받은 디바이스 토큰을 users.fcm_token에 저장합니다. " +
                          "같은 사용자가 다시 호출하면 기존 토큰을 새 토큰으로 덮어씁니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록/갱신 성공 — data는 null"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "fcmToken 누락/공백 또는 500자 초과 (E000)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (E401)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음 (A005)")
    })
    ResponseEntity<ApiResponse<Void>> updateFcmToken(CustomUserDetails userDetails, FcmTokenUpdateRequest request);
}
