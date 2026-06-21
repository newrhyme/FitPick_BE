package com.fitpick.global.infra.s3;

import com.fitpick.domain.user.exception.ImageErrorCode;
import com.fitpick.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;
    private final S3Properties props;

    public String upload(MultipartFile file, String key) {
        try (InputStream is = file.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(props.bucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(is, file.getSize()));
            return props.publicBaseUrl() + "/" + key;
        } catch (IOException e) {
            log.error("S3 업로드 중 InputStream 읽기 실패. key={}", key, e);
            throw new CustomException(ImageErrorCode.UPLOAD_FAILED);
        } catch (RuntimeException e) {
            log.error("S3 업로드 실패. key={}", key, e);
            throw new CustomException(ImageErrorCode.UPLOAD_FAILED);
        }
    }
}
