package com.fitpick.global.infra.openai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Slf4j
@Component
public class ImageDownloader {

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 30_000;

    private final RestClient client;

    public ImageDownloader() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(CONNECT_TIMEOUT_MS));
        factory.setReadTimeout(Duration.ofMillis(READ_TIMEOUT_MS));
        this.client = RestClient.builder().requestFactory(factory).build();
    }

    public ImageInput download(String url, String fallbackFilename) {
        byte[] bytes = client.get().uri(url).retrieve().body(byte[].class);
        if (bytes == null || bytes.length == 0) {
            throw new IllegalStateException("이미지 다운로드 실패(빈 응답): " + url);
        }
        String contentType = client.head().uri(url).retrieve().toBodilessEntity()
                .getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        if (contentType == null || contentType.isBlank()) {
            contentType = "image/png";
        }
        return new ImageInput(fallbackFilename, contentType, bytes);
    }
}
