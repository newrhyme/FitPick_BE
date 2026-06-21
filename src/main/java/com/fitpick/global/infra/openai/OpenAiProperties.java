package com.fitpick.global.infra.openai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.openai")
public record OpenAiProperties(
        String apiKey,
        String baseUrl,
        String imageModel,
        String imageSize,
        String imageQuality,
        Integer timeoutSec
) {
}
