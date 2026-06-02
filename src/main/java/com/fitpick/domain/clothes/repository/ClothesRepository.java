package com.fitpick.domain.clothes.repository;

import com.fitpick.domain.clothes.entity.Clothes;
import com.fitpick.domain.clothes.entity.ClothesCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClothesRepository extends JpaRepository<Clothes, Long> {

    // 목록 조회: 활성 상품 전체 (페이징 + 최신순)
    Page<Clothes> findByIsActiveTrue(Pageable pageable);

    // 목록 조회: 활성 상품 + 카테고리 필터 (페이징 + 최신순)
    Page<Clothes> findByIsActiveTrueAndCategory(ClothesCategory category, Pageable pageable);

    // 상세 조회: 옷 + 옵션 fetch join (단건이라 fetch join 문제없음)
    @Query("SELECT c FROM Clothes c LEFT JOIN FETCH c.options WHERE c.id = :id")
    Optional<Clothes> findByIdWithOptions(@Param("id") Long id);
}
