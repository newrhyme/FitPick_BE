package com.fitpick.domain.clothes.entity;

public enum StockStatus {
    AVAILABLE, // 구매 가능
    LOW_STOCK, // 재고 임박
    SOLD_OUT;  // 품절

    public static final int LOW_STOCK_THRESHOLD = 5;

    // 재고 수량 → 상태 변환. 임계치(threshold)는 LOW_STOCK 판정 기준.
    public static StockStatus of(int stockQuantity) {
        if (stockQuantity <= 0) {
            return SOLD_OUT;
        }

        // 1차에서는 LOW_STOCK을 쓰지 않으므로 0 초과면 전부 AVAILABLE.
        // 추후 LOW_STOCK 도입 시: if (stockQuantity <= LOW_STOCK_THRESHOLD) return LOW_STOCK;
        if (stockQuantity <= LOW_STOCK_THRESHOLD)
            return LOW_STOCK;

        return AVAILABLE;
    }

}
