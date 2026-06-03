package com.fitpick.domain.nfc.service;

import com.fitpick.domain.clothes.dto.ClothesDetailResponse;

public interface NfcService {
    ClothesDetailResponse getClothesByTagUid(String tagUid, Long userId);
}
