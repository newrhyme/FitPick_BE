package com.fitpick.domain.order.dto;

import com.fitpick.domain.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "주문 상태 변경 요청 (관리자 전용)")
public record OrderStatusUpdateRequest(

        @Schema(
                description = "변경할 주문 상태. 허용 전이: PAID→PREPARING→READY→PICKED_UP, "
                        + "또는 PAID/PREPARING/READY → CANCELED (관리자 취소, 재고 복구 동반)",
                example = "PREPARING",
                allowableValues = {"PREPARING", "READY", "PICKED_UP", "CANCELED"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "변경할 상태는 필수입니다.")
        OrderStatus status,

        @Schema(
                description = "고객에게 보낼 알림 본문(코멘트). 상태 변경 시 자동 발송되는 푸시 알림의 body로 사용됨. "
                        + "예: '주문이 수락되었습니다. 잠시만 기다려주세요', '상품이 준비되었습니다. 입구에서 수령하세요', "
                        + "'수령완료되었습니다. 환불은 7일 이내 가능합니다'.",
                example = "상품이 준비되었습니다. 입구에서 수령하세요.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "알림 코멘트는 필수입니다.")
        @Size(max = 500)
        String comment
) {
}
