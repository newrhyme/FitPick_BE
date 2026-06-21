package com.fitpick.domain.tryon.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "try_on_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class TryOnItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "try_on_id", nullable = false)
    private TryOn tryOn;

    @Column(name = "clothes_option_id", nullable = false)
    private Long clothesOptionId;

    public static TryOnItem of(Long clothesOptionId) {
        return TryOnItem.builder()
                .clothesOptionId(clothesOptionId)
                .build();
    }

    void assignTryOn(TryOn tryOn) {
        this.tryOn = tryOn;
    }
}
