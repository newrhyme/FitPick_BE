package com.fitpick.domain.clothes.repository;

import com.fitpick.domain.clothes.entity.ClothesImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClothesImageRepository extends JpaRepository<ClothesImage, Long> {

    // 특정 옷 이미지 sort_order 순으로 조회
    List<ClothesImage> findByClothesIdOrderBySortOrderAsc(Long clothesId);
}
