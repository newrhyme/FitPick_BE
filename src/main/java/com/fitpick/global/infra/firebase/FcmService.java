package com.fitpick.global.infra.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class FcmService {

    // FirebaseConfig 조건 미충족 시 빈이 없을 수 있음 → required=false로 null 허용.
    private final FirebaseMessaging firebaseMessaging;

    public FcmService(@Autowired(required = false) FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
        if (firebaseMessaging == null) {
            log.warn("FCM 비활성화 — FirebaseMessaging 빈 없음. send() 호출은 no-op.");
        } else {
            log.info("FCM 활성화 — FirebaseMessaging 주입 완료.");
        }
    }

    // 발송 실패해도 호출 트랜잭션에 영향 없도록 예외 던지지 않음. 결과 객체로 보고.
    public FcmSendResult send(String token, String title, String body, Map<String, String> data) {
        if (firebaseMessaging == null) {
            log.warn("FCM 비활성화 상태 — 발송 skip: title={}", title);
            return FcmSendResult.skipped("FCM_DISABLED");
        }
        if (token == null || token.isBlank()) {
            log.warn("FCM 토큰 없음 — 발송 skip: title={}", title);
            return FcmSendResult.skipped("TOKEN_EMPTY");
        }
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data != null ? data : Map.of())
                    .build();

            String messageId = firebaseMessaging.send(message);
            log.info("FCM 발송 성공: token={}..., messageId={}", maskToken(token), messageId);
            return FcmSendResult.sent(messageId);
        } catch (Exception e) {
            log.error("FCM 발송 실패: token={}..., error={}", maskToken(token), e.getMessage());
            return FcmSendResult.failed(e.getMessage());
        }
    }

    private static String maskToken(String token) {
        return token.length() <= 10 ? token : token.substring(0, 10);
    }

    public record FcmSendResult(boolean sent, String messageId, String reason) {
        public static FcmSendResult sent(String messageId) {
            return new FcmSendResult(true, messageId, null);
        }
        public static FcmSendResult skipped(String reason) {
            return new FcmSendResult(false, null, reason);
        }
        public static FcmSendResult failed(String reason) {
            return new FcmSendResult(false, null, reason);
        }
    }
}
