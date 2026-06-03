package com.fitpick.domain.viewhistory.entity;

import com.fitpick.domain.clothes.entity.Clothes;
import com.fitpick.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "view_histories")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ViewHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 누가 봤는지. 로그인 사용자만 적재되므로 항상 존재.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 무엇을 봤는지.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothes_id", nullable = false)
    private Clothes clothes;

    // 진입 경로. NFC_TAG(태깅) / DETAIL_VIEW(상세 직접 조회)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ViewSource source;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ViewHistory create(User user, Clothes clothes, ViewSource source) {
        return ViewHistory.builder()
                .user(user)
                .clothes(clothes)
                .source(source)
                .build();
    }

}
