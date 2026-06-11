package com.fitpick.domain.order.dto;

public record AdminOrderStatsResponse(
        Long todayTotalCount,
        Long paidCount,
        Long preparingCount,
        Long readyCount,
        Long pickedUpCount,
        Long canceledCount
) {
}