package com.fitpick.domain.tryon.controller;

import com.fitpick.domain.tryon.controller.docs.TryOnApiDocs;
import com.fitpick.domain.tryon.dto.TryOnCreateRequest;
import com.fitpick.domain.tryon.dto.TryOnListItemResponse;
import com.fitpick.domain.tryon.dto.TryOnResponse;
import com.fitpick.domain.tryon.service.TryOnService;
import com.fitpick.global.common.code.SuccessCode;
import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/try-ons")
public class TryOnController implements TryOnApiDocs {

    private final TryOnService tryOnService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TryOnCreateRequest request
    ) {
        TryOnResponse response = tryOnService.create(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.CREATED, response));
    }

    @GetMapping("/{tryOnId}")
    public ApiResponse<?> get(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long tryOnId
    ) {
        TryOnResponse response = tryOnService.get(userDetails.getUserId(), tryOnId);
        return ApiResponse.success(SuccessCode.READ_SUCCESS, response);
    }

    @GetMapping
    public ApiResponse<?> getMyList(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<TryOnListItemResponse> response = tryOnService.getMyList(userDetails.getUserId());
        return ApiResponse.success(SuccessCode.READ_SUCCESS, response);
    }
}
