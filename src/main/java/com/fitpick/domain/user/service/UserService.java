package com.fitpick.domain.user.service;

import com.fitpick.domain.user.dto.UserMeResponse;

public interface UserService {

    UserMeResponse getMyInfo(Long userId);
}
