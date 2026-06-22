package com.fitpick.global.infra.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@EnableConfigurationProperties(FirebaseProperties.class)
@Slf4j
public class FirebaseConfig {

    // 키 파일이 있고 enabled=true일 때만 FirebaseApp 빈을 등록.
    // 키 없으면 빈 자체가 생성되지 않아 FcmService는 no-op으로 동작 → 부팅은 통과.
    @Bean
    @Conditional(FirebaseAvailableCondition.class)
    public FirebaseApp firebaseApp(FirebaseProperties props) throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }
        try (FileInputStream serviceAccount = new FileInputStream(props.credentialsPath())) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Firebase 초기화 완료 (path={}).", props.credentialsPath());
            return app;
        }
    }

    @Bean
    @ConditionalOnBean(FirebaseApp.class)
    public FirebaseMessaging firebaseMessaging(FirebaseApp app) {
        return FirebaseMessaging.getInstance(app);
    }

    // enabled=true + credentialsPath 비어있지 않음 + 파일 존재 — 셋 다 만족해야 빈 생성.
    static class FirebaseAvailableCondition implements Condition {
        private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(FirebaseConfig.class);

        @Override
        public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata md) {
            String enabled = ctx.getEnvironment().getProperty("app.firebase.enabled", "true");
            if (!"true".equalsIgnoreCase(enabled)) {
                LOG.warn("Firebase 비활성화 (app.firebase.enabled=false). FCM 발송 skip.");
                return false;
            }
            String path = ctx.getEnvironment().getProperty("app.firebase.credentials-path");
            if (path == null || path.isBlank()) {
                LOG.warn("Firebase 키 경로 미설정 (app.firebase.credentials-path). FCM 발송 skip.");
                return false;
            }
            if (!new File(path).exists()) {
                LOG.warn("Firebase 키 파일 없음 (path={}). FCM 발송 skip.", path);
                return false;
            }
            return true;
        }
    }
}
