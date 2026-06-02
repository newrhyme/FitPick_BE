package com.example.springbootboilerplate.global.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
@Component
public class RequestIdInterceptor implements HandlerInterceptor {

    private static final String REQUEST_ID = "requestId";
    private static final String HEADER_REQUEST_ID = "X-Request-ID";
    private static final String START_TIME = "startTime";

    // Request ID를 생성하고 로깅 컨텍스트에 추가
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        // Request Header에서 Request ID 추출 또는 새로 생성
        String requestId = request.getHeader(HEADER_REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        // 로깅 컨텍스트에 Request ID 추가
        MDC.put(REQUEST_ID, requestId);
        request.setAttribute(START_TIME, System.currentTimeMillis());
        response.setHeader(HEADER_REQUEST_ID, requestId);

        return true;
    }

    // 요청 완료 후 로그 기록 및 로깅 컨텍스트 정리
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {

        // 요청 처리 시간 계산 및 로그 기록
        Long startTime = (Long) request.getAttribute(START_TIME);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;

            log.info("method={}, uri={}, status={}, duration={}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration);
        }
        MDC.clear();
    }
}
