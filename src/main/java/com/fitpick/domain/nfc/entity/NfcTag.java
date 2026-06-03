package com.fitpick.domain.nfc.entity;

import com.fitpick.domain.clothes.entity.Clothes;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothes_id", nullable = false, unique = true)
    private Clothes clothes;

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
