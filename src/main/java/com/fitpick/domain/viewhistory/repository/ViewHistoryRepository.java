package com.fitpick.domain.viewhistory.repository;

import com.fitpick.domain.viewhistory.entity.ViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {

    // 적재(save)만 필요. 기본 JpaRepository로 충분.
    // 추천 기능 만들 때 findByUserId... 같은 조회 메서드 추가 예정.
}
