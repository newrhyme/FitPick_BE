package com.fitpick.domain.viewhistory.service;

import com.fitpick.domain.clothes.entity.Clothes;
import com.fitpick.domain.user.entity.User;
import com.fitpick.domain.user.repository.UserRepository;
import com.fitpick.domain.viewhistory.entity.ViewHistory;
import com.fitpick.domain.viewhistory.entity.ViewSource;
import com.fitpick.domain.viewhistory.repository.ViewHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewHistoryServiceImpl implements ViewHistoryService {

    private final ViewHistoryRepository viewHistoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId, Clothes clothes, ViewSource source) {
        if (userId == null) {
            return;
        }
        try {
            User userRef = userRepository.getReferenceById(userId);
            viewHistoryRepository.save(ViewHistory.create(userRef, clothes, source));
        } catch (Exception e) {
            log.warn("view_history 적재 실패: userId={}, clothesId={}, source={}",
                    userId, clothes.getId(), source, e);
        }
    }
}
