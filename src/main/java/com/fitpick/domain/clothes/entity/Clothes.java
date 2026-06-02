package com.fitpick.domain.clothes.entity;

import com.fitpick.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clothes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Clothes extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "clothes", fetch = FetchType.LAZY)
    private List<ClothesOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "clothes", fetch = FetchType.LAZY)
    private List<ClothesImage> images = new ArrayList<>();

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ClothesCategory category;

    @Column(length = 100)
    private String material;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "thumbnail_image_url", length = 500)
    private String thumbnailImageUrl;

    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isActive;

    public static Clothes create(Long storeId, String title, String description,
                                 ClothesCategory category, String material, Integer price,
                                 String thumbnailImageUrl) {
        return Clothes.builder()
                .storeId(storeId)
                .title(title)
                .description(description)
                .category(category)
                .material(material)
                .price(price)
                .thumbnailImageUrl(thumbnailImageUrl)
                .isActive(true)   // 생성 시 기본 판매 상태
                .build();
    }
}
