package com.fitpick.domain.tryon.service;

import com.fitpick.domain.tryon.dto.TryOnCreateRequest;
import com.fitpick.domain.tryon.dto.TryOnListItemResponse;
import com.fitpick.domain.tryon.dto.TryOnResponse;

import java.util.List;

public interface TryOnService {

    TryOnResponse create(Long userId, TryOnCreateRequest request);

    TryOnResponse get(Long userId, Long tryOnId);

    List<TryOnListItemResponse> getMyList(Long userId);
}
