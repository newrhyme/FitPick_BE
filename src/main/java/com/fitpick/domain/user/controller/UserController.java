package com.fitpick.domain.user.controller;

import com.fitpick.domain.user.controller.docs.UserApiDocs;
import com.fitpick.domain.user.dto.FcmTokenUpdateRequest;
import com.fitpick.domain.user.dto.UserMeResponse;
import com.fitpick.domain.user.dto.UserUpdateRequest;
import com.fitpick.domain.user.service.UserImageService;
import com.fitpick.domain.user.service.UserService;
import com.fitpick.global.common.code.SuccessCode;
import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController implements UserApiDocs {

    private final UserService userService;
    private final UserImageService userImageService;

    @GetMapping("/me")
    public ApiResponse<?> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserMeResponse response = userService.getMyInfo(userDetails.getUserId());
        return ApiResponse.success(SuccessCode.READ_SUCCESS, response);
    }

    @PatchMapping("/me")
    public ApiResponse<?> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        UserMeResponse response = userService.updateMyInfo(userDetails.getUserId(), request);
        return ApiResponse.success(SuccessCode.UPDATE_SUCCESS, response);
    }

    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> uploadProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("file") MultipartFile file
    ) {
        userImageService.uploadProfileImage(userDetails.getUserId(), file);
        UserMeResponse response = userService.getMyInfo(userDetails.getUserId());
        return ApiResponse.success(SuccessCode.UPDATE_SUCCESS, response);
    }

    @PostMapping(value = "/me/try-on-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> uploadTryOnImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("file") MultipartFile file
    ) {
        userImageService.uploadTryOnImage(userDetails.getUserId(), file);
        UserMeResponse response = userService.getMyInfo(userDetails.getUserId());
        return ApiResponse.success(SuccessCode.UPDATE_SUCCESS, response);
    }

    @PostMapping("/me/fcm-token")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FcmTokenUpdateRequest request
    ) {
        userService.updateFcmToken(userDetails.getUserId(), request.fcmToken());
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.UPDATE_SUCCESS));
    }
}
