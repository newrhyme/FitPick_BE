package com.fitpick.global.infra.firebase;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.firebase")
public record FirebaseProperties(
        String credentialsPath,
        boolean enabled
) {
}
