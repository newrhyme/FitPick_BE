package com.fitpick.domain.order.repository;

import com.fitpick.domain.order.entity.OrderStatus;

public interface OrderStatusCountProjection {
    OrderStatus getStatus();
    Long getCount();
}
