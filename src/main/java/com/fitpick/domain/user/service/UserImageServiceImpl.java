package com.fitpick.domain.user.service;

import com.fitpick.domain.auth.exception.AuthErrorCode;
import com.fitpick.domain.user.entity.User;
import com.fitpick.domain.user.exception.ImageErrorCode;
import com.fitpick.domain.user.repository.UserRepository;
import com.fitpick.global.exception.CustomException;
import com.fitpick.global.infra.s3.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserImageServiceImpl implements UserImageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private static final Map<String, String> CONTENT_TYPE_TO_EXT = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;

    @Override
    @Transactional
    public String uploadProfileImage(Long userId, MultipartFile file) {
        String key = buildKey(userId, "profile", file);
        String url = s3Uploader.upload(file, key);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
        user.updateProfileImageUrl(url);

        return url;
    }

    @Override
    @Transactional
    public String uploadTryOnImage(Long userId, MultipartFile file) {
        String key = buildKey(userId, "try-on", file);
        String url = s3Uploader.upload(file, key);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
        user.updateTryOnImageUrl(url);

        return url;
    }

    private String buildKey(Long userId, String category, MultipartFile file) {
        validate(file);
        String contentType = file.getContentType().toLowerCase();
        String ext = CONTENT_TYPE_TO_EXT.get(contentType);
        return "users/" + userId + "/" + category + "/" + UUID.randomUUID() + "." + ext;
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ImageErrorCode.EMPTY_FILE);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new CustomException(ImageErrorCode.UNSUPPORTED_TYPE);
        }
    }
}
