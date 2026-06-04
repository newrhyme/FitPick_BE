package com.fitpick.domain.clothes.repository;

import com.fitpick.domain.clothes.entity.ClothesOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClothesOptionRepository extends JpaRepository<ClothesOption, Long> {

    // 특정 옷 옵션 전체 조회 (상세 페이지 색상, 사이즈, 재고 보여줄 때)
    List<ClothesOption> findByClothesId(Long clothesId);

    // 재고 실제 차감 : stock >= quantity일 때만 차감
    // 영향 받은 행 수 == 1 성공
    // else 재고 부족
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ClothesOption o
            SET o.stockQuantity = o.stockQuantity - :quantity
            WHERE o.id = :optionId AND o.stockQuantity >= :quantity
            """)
    int decreaseStock(@Param("optionId") Long optionId, @Param("quantity") int quantity);

    // 재고 복구 (취소 시)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ClothesOption o
            SET o.stockQuantity = o.stockQuantity + :quantity
            WHERE o.id = :optionId
            """)
    int increaseStock(@Param("optionId") Long optionId, @Param("quantity") int quantity);
}
