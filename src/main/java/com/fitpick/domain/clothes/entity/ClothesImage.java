package com.fitpick.domain.clothes.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "clothes_images")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ClothesImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothes_id", nullable = false)
    private Clothes clothes;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    // clothes_images 테이블엔 updated_at이 없어 BaseTimeEntity를 상속하지 않고
    // created_at만 직접 선언한다.
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ClothesImage create(Clothes clothes, String imageUrl, Integer sortOrder) {
        return ClothesImage.builder()
                .clothes(clothes)
                .imageUrl(imageUrl)
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .build();
    }
}
