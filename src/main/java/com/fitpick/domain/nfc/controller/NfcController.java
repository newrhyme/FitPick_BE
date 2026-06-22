package com.fitpick.domain.nfc.controller;

import com.fitpick.domain.nfc.controller.docs.NfcApiDocs;
import com.fitpick.domain.nfc.dto.NfcTagResponse;
import com.fitpick.domain.nfc.service.NfcService;
import com.fitpick.global.common.code.SuccessCode;
import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clothes/nfc")
public class NfcController implements NfcApiDocs {

    private final NfcService nfcService;

    @GetMapping("/{tagUid}")
    public ApiResponse<?> getClothesByTagUid (
            @PathVariable String tagUid,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        log.info("NFC 조회 userId = {}", userId);
        NfcTagResponse response = nfcService.getClothesByTagUid(tagUid, userId);
        return ApiResponse.success(SuccessCode.READ_SUCCESS, response);
    }
}
