package com.fitpick.domain.nfc.service;

import com.fitpick.domain.clothes.dto.ClothesDetailResponse;
import com.fitpick.domain.clothes.entity.Clothes;
import com.fitpick.domain.clothes.entity.ClothesImage;
import com.fitpick.domain.clothes.entity.ClothesOption;
import com.fitpick.domain.clothes.exception.ClothesErrorCode;
import com.fitpick.domain.clothes.repository.ClothesImageRepository;
import com.fitpick.domain.clothes.repository.ClothesRepository;
import com.fitpick.domain.nfc.entity.NfcTag;
import com.fitpick.domain.nfc.exception.NfcErrorCode;
import com.fitpick.domain.nfc.repository.NfcTagRepository;
import com.fitpick.domain.viewhistory.entity.ViewSource;
import com.fitpick.domain.viewhistory.service.ViewHistoryService;
import com.fitpick.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NfcServiceImpl implements NfcService {

    private final NfcTagRepository nfcTagRepository;
    private final ClothesRepository clothesRepository;
    private final ClothesImageRepository clothesImageRepository;
    private final ViewHistoryService viewHistoryService;

    @Override
    public ClothesDetailResponse getClothesByTagUid(String tagUid, Long userId) {
        // 1) 활성 태그 조회 (없거나 비활성이면 예외)
        NfcTag nfcTag = nfcTagRepository.findByTagUidAndIsActiveTrue(tagUid)
                .orElseThrow(() -> new CustomException(NfcErrorCode.TAG_NOT_FOUND));

        Long clothesId = nfcTag.getClothes().getId();

        // 2) 옷 + 옵션 (fetch join) — 활성 상품만 노출
        Clothes clothes = clothesRepository.findByIdWithOptions(clothesId)
                .orElseThrow(() -> new CustomException(ClothesErrorCode.CLOTHES_NOT_FOUND));
        if (!clothes.getIsActive()) {
            throw new CustomException(ClothesErrorCode.CLOTHES_NOT_FOUND);
        }
        List<ClothesOption> options = clothes.getOptions();

        // 3) 이미지
        List<ClothesImage> images =
                clothesImageRepository.findByClothesIdOrderBySortOrderAsc(clothesId);

        // 4) 조회 이력 적재 (NFC_TAG) — 로그인 시에만, 실패해도 조회는 성공
        viewHistoryService.record(userId, clothes, ViewSource.NFC_TAG);

        return ClothesDetailResponse.of(clothes, options, images);
    }
}
