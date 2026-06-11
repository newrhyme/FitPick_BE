package com.fitpick.domain.user.controller;

import com.fitpick.domain.user.controller.docs.UserApiDocs;
import com.fitpick.domain.user.dto.UserMeResponse;
import com.fitpick.domain.user.service.UserService;
import com.fitpick.global.common.code.SuccessCode;
import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController implements UserApiDocs {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<?> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserMeResponse response = userService.getMyInfo(userDetails.getUserId());
        return ApiResponse.success(SuccessCode.READ_SUCCESS, response);
    }
}
