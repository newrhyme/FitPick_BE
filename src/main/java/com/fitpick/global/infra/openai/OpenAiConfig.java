package com.fitpick.global.infra.openai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfig {

    private static final String DEFAULT_BASE_URL = "https://api.openai.com";
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 10_000;
    private static final int DEFAULT_READ_TIMEOUT_SEC = 60;

    @Bean
    public RestClient openAiRestClient(OpenAiProperties props) {
        int readTimeoutSec = props.timeoutSec() != null ? props.timeoutSec() : DEFAULT_READ_TIMEOUT_SEC;
        String baseUrl = props.baseUrl() != null && !props.baseUrl().isBlank() ? props.baseUrl() : DEFAULT_BASE_URL;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(DEFAULT_CONNECT_TIMEOUT_MS));
        factory.setReadTimeout(Duration.ofSeconds(readTimeoutSec));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }
}
