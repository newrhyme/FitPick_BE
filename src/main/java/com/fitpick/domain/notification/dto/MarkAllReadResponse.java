package com.fitpick.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "전체 알림 읽음 처리 응답")
public record MarkAllReadResponse(

        @Schema(description = "이번 호출로 읽음 처리된 알림 수", example = "3")
        int updatedCount
) {
}
