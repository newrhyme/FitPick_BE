package com.fitpick.domain.clothes.controller;

import com.fitpick.domain.clothes.controller.docs.ClothesApiDocs;
import com.fitpick.domain.clothes.dto.ClothesDetailResponse;
import com.fitpick.domain.clothes.dto.ClothesListResponse;
import com.fitpick.domain.clothes.entity.ClothesCategory;
import com.fitpick.domain.clothes.service.ClothesService;
import com.fitpick.global.common.code.SuccessCode;
import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.common.response.PageResponse;
import com.fitpick.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clothes")
public class ClothesController implements ClothesApiDocs {

    private final ClothesService clothesService;

    @GetMapping
    public ApiResponse<?> getClothesList(
            @RequestParam(required = false) ClothesCategory category,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<ClothesListResponse> response = clothesService.getClothesList(category, pageable);
        return ApiResponse.success(SuccessCode.READ_SUCCESS, response);
    }

    @GetMapping("/{clothesId}")
    public ApiResponse<?> getClothesDetail(
            @PathVariable Long clothesId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        ClothesDetailResponse response = clothesService.getClothesDetail(clothesId, userId);
        return ApiResponse.success(SuccessCode.READ_SUCCESS, response);
    }
}
