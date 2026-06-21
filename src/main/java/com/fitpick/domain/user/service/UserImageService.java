package com.fitpick.domain.user.service;

import org.springframework.web.multipart.MultipartFile;

public interface UserImageService {

    String uploadProfileImage(Long userId, MultipartFile file);

    String uploadTryOnImage(Long userId, MultipartFile file);
}
