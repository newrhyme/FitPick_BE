package com.fitpick.domain.tryon.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "try_ons")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class TryOn {

    private static final int FAILURE_REASON_MAX_LENGTH = 500;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "original_image_url", nullable = false, length = 500)
    private String originalImageUrl;

    @Column(name = "product_image_url", length = 500)
    private String productImageUrl;

    @Column(name = "generated_image_url", length = 500)
    private String generatedImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TryOnStatus status;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "tryOn", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TryOnItem> items = new ArrayList<>();

    public static TryOn createProcessing(Long userId, String originalImageUrl, String productImageUrl) {
        return TryOn.builder()
                .userId(userId)
                .originalImageUrl(originalImageUrl)
                .productImageUrl(productImageUrl)
                .status(TryOnStatus.PROCESSING)
                .build();
    }

    public void addItem(TryOnItem item) {
        this.items.add(item);
        item.assignTryOn(this);
    }

    public void markDone(String generatedImageUrl) {
        this.status = TryOnStatus.DONE;
        this.generatedImageUrl = generatedImageUrl;
        this.failureReason = null;
    }

    public void markFailed(String reason) {
        this.status = TryOnStatus.FAILED;
        if (reason != null && reason.length() > FAILURE_REASON_MAX_LENGTH) {
            reason = reason.substring(0, FAILURE_REASON_MAX_LENGTH);
        }
        this.failureReason = reason;
    }
}
