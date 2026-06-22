package com.fitpick.domain.nfc.service;

import com.fitpick.domain.clothes.entity.Clothes;
import com.fitpick.domain.clothes.entity.ClothesOption;
import com.fitpick.domain.clothes.exception.ClothesErrorCode;
import com.fitpick.domain.clothes.repository.ClothesRepository;
import com.fitpick.domain.nfc.dto.NfcTagResponse;
import com.fitpick.domain.nfc.entity.NfcTag;
import com.fitpick.domain.nfc.exception.NfcErrorCode;
import com.fitpick.domain.nfc.repository.NfcTagRepository;
import com.fitpick.domain.viewhistory.entity.ViewSource;
import com.fitpick.domain.viewhistory.service.ViewHistoryService;
import com.fitpick.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NfcServiceImpl implements NfcService {

    private final NfcTagRepository nfcTagRepository;
    private final ClothesRepository clothesRepository;
    private final ViewHistoryService viewHistoryService;

    @Override
    public NfcTagResponse getClothesByTagUid(String tagUid, Long userId) {
        // 1) 활성 태그 조회 — 없거나 비활성이면 404 N001
        NfcTag nfcTag = nfcTagRepository.findByTagUidAndIsActiveTrue(tagUid)
                .orElseThrow(() -> new CustomException(NfcErrorCode.TAG_NOT_FOUND));

        // 2) 옷 활성 여부 확인
        Clothes clothes = nfcTag.getClothes();
        if (!clothes.getIsActive()) {
            throw new CustomException(ClothesErrorCode.CLOTHES_NOT_FOUND);
        }

        // 3) 옵션 결정
        //    - 옵션 단위 태그(clothes_option_id 채움) → 그 옵션
        //    - 옷 단위 태그(NULL) → 첫 번째 옵션으로 폴백
        ClothesOption option = (nfcTag.getClothesOption() != null)
                ? nfcTag.getClothesOption()
                : clothesRepository.findByIdWithOptions(clothes.getId())
                        .orElseThrow(() -> new CustomException(ClothesErrorCode.CLOTHES_NOT_FOUND))
                        .getOptions().stream()
                        .findFirst()
                        .orElseThrow(() -> new CustomException(ClothesErrorCode.CLOTHES_OPTION_NOT_FOUND));

        // 4) 조회 이력 적재 (NFC_TAG) — 로그인 시에만, 실패해도 조회는 성공
        viewHistoryService.record(userId, clothes, ViewSource.NFC_TAG);

        return NfcTagResponse.of(clothes, option);
    }
}
