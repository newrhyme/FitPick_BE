package com.fitpick.domain.tryon.service;

import com.fitpick.domain.tryon.entity.TryOn;

public interface TryOnPersistenceService {

    TryOn createProcessing(Long userId, String originalImageUrl, String productImageUrl, Long clothesOptionId, String style);

    TryOn markDone(Long tryOnId, String generatedImageUrl);

    TryOn markFailed(Long tryOnId, String reason);
}
