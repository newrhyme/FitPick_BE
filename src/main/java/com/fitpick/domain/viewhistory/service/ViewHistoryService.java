package com.fitpick.domain.viewhistory.service;

import com.fitpick.domain.clothes.entity.Clothes;
import com.fitpick.domain.viewhistory.entity.ViewSource;

public interface ViewHistoryService {
    void record(Long userId, Clothes clothes, ViewSource source);
}
