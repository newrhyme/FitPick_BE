package com.fitpick.domain.tryon.service;

import com.fitpick.domain.tryon.entity.TryOn;
import com.fitpick.domain.tryon.entity.TryOnItem;
import com.fitpick.domain.tryon.exception.TryOnErrorCode;
import com.fitpick.domain.tryon.repository.TryOnRepository;
import com.fitpick.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TryOnPersistenceServiceImpl implements TryOnPersistenceService {

    private final TryOnRepository tryOnRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TryOn createProcessing(Long userId, String originalImageUrl, String productImageUrl, Long clothesOptionId, String style) {
        TryOn tryOn = TryOn.createProcessing(userId, originalImageUrl, productImageUrl, style);
        tryOn.addItem(TryOnItem.of(clothesOptionId));
        return tryOnRepository.save(tryOn);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TryOn markDone(Long tryOnId, String generatedImageUrl) {
        TryOn tryOn = tryOnRepository.findById(tryOnId)
                .orElseThrow(() -> new CustomException(TryOnErrorCode.TRY_ON_NOT_FOUND));
        tryOn.markDone(generatedImageUrl);
        return tryOn;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TryOn markFailed(Long tryOnId, String reason) {
        TryOn tryOn = tryOnRepository.findById(tryOnId)
                .orElseThrow(() -> new CustomException(TryOnErrorCode.TRY_ON_NOT_FOUND));
        tryOn.markFailed(reason);
        return tryOn;
    }
}
