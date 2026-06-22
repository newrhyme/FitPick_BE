package com.fitpick.domain.nfc.entity;

import com.fitpick.domain.clothes.entity.Clothes;
import com.fitpick.domain.clothes.entity.ClothesOption;
import com.fitpick.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nfc_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class NfcTag extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 같은 옷에 옵션별 태그를 다수 부착하기 위해 ManyToOne. (UNIQUE 제약 0018에서 해제)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothes_id", nullable = false)
    private Clothes clothes;

    // 옵션 단위 태그면 채워짐. NULL이면 옷 단위 태그(레거시) → 서비스에서 첫 옵션으로 폴백.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothes_option_id")
    private ClothesOption clothesOption;

    // 실제 NFC 태그에서 읽어오는 고유 식별값. 조회 진입 키라 UNIQUE.
    @Column(name = "tag_uid", nullable = false, unique = true, length = 100)
    private String tagUid;

    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isActive;

    public static NfcTag create(Clothes clothes, String tagUid) {
        return NfcTag.builder()
                .clothes(clothes)
                .tagUid(tagUid)
                .isActive(true)
                .build();
    }
}
