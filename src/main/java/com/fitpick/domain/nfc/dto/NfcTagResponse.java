package com.fitpick.domain.nfc.dto;

import com.fitpick.domain.clothes.entity.Clothes;
import com.fitpick.domain.clothes.entity.ClothesOption;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "NFC 태그 조회 응답 — 옷+옵션 단위로 식별. 안드로이드는 이 결과로 상품 상세 진입 시 해당 옵션을 미리 선택된 상태로 표시.")
public record NfcTagResponse(

        @Schema(description = "상품 ID", example = "1")
        Long clothesId,

        @Schema(description = "옵션 ID", example = "41")
        Long clothesOptionId,

        @Schema(description = "상품명", example = "에어리즘 코튼 오버사이즈 티셔츠")
        String title,

        @Schema(description = "사이즈", example = "M")
        String size,

        @Schema(description = "색상", example = "화이트")
        String color
) {
    public static NfcTagResponse of(Clothes clothes, ClothesOption option) {
        return new NfcTagResponse(
                clothes.getId(),
                option.getId(),
                clothes.getTitle(),
                option.getSize(),
                option.getColor()
        );
    }
}
