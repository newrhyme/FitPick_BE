package com.fitpick.domain.user.service;

import com.fitpick.domain.user.dto.UserMeResponse;
import com.fitpick.domain.user.dto.UserUpdateRequest;

public interface UserService {

    UserMeResponse getMyInfo(Long userId);

    UserMeResponse updateMyInfo(Long userId, UserUpdateRequest request);
}
