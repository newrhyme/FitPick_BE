package com.fitpick.domain.clothes.repository;

import com.fitpick.domain.clothes.entity.ClothesOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClothesOptionRepository extends JpaRepository<ClothesOption, Long> {

    // 특정 옷 옵션 전체 조회 (상세 페이지 색상, 사이즈, 재고 보여줄 때)
    List<ClothesOption> findByClothesId(Long clothesId);
}
