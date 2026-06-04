package com.fitpick.domain.order.service;

import com.fitpick.domain.cart.entity.Cart;
import com.fitpick.domain.cart.entity.CartItem;
import com.fitpick.domain.cart.repository.CartItemRepository;
import com.fitpick.domain.cart.repository.CartRepository;
import com.fitpick.domain.clothes.entity.ClothesOption;
import com.fitpick.domain.clothes.repository.ClothesOptionRepository;
import com.fitpick.domain.order.dto.DirectOrderRequest;
import com.fitpick.domain.order.dto.OrderResponse;
import com.fitpick.domain.order.dto.OrderSummaryResponse;
import com.fitpick.domain.order.entity.Order;
import com.fitpick.domain.order.entity.OrderItem;
import com.fitpick.domain.order.entity.OrderStatus;
import com.fitpick.domain.order.entity.OrderType;
import com.fitpick.domain.order.exception.OrderErrorCode;
import com.fitpick.domain.order.repository.OrderRepository;
import com.fitpick.global.common.response.PageResponse;
import com.fitpick.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    private static final Long DEFAULT_STORE_ID = 1L;

    private final OrderRepository orderRepository;
    private final ClothesOptionRepository clothesOptionRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    // === 공통 헬퍼: 옵션 1건 → 가격 스냅샷 + 원자적 재고 차감 → 주문아이템 ===
    // CART는 루프로, DIRECT는 1회 호출. 차감 로직을 한 곳에 모음.
    private OrderItem buildItemAndDecreaseStock(Long optionId, Integer quantity) {
        // 1) 옵션 풀 로드 (clothes까지 — 가격 스냅샷 위해)
        ClothesOption option = clothesOptionRepository.findById(optionId)
                .orElseThrow(() -> new CustomException(OrderErrorCode.CLOTHES_OPTION_NOT_FOUND));

        // 2) 가격 스냅샷: 옵션별 가격 없음 → clothes.price 복사
        Integer price = option.getClothes().getPrice();   // ※ Clothes.price 타입이 Integer라는 가정

        // 3) 원자적 재고 차감: 영향 행 0이면 재고부족
        int affected = clothesOptionRepository.decreaseStock(optionId, quantity);
        if (affected == 0) {
            throw new CustomException(OrderErrorCode.OUT_OF_STOCK);
        }

        // 4) 주문아이템 생성 (order 연관은 order.addItem에서 세팅)
        return OrderItem.create(option, quantity, price);
    }

    @Override
    @Transactional
    public OrderResponse orderFromCart(Long userId) {
        // 1) 내 장바구니 조회 (없거나 비어 있으면 주문 불가)
        Cart cart = cartRepository.findByUserIdAndStoreId(userId, DEFAULT_STORE_ID)
                .orElseThrow(() -> new CustomException(OrderErrorCode.CART_EMPTY));

        List<CartItem> cartItems = cart.getItems();
        if (cartItems.isEmpty()) {
            throw new CustomException(OrderErrorCode.CART_EMPTY);
        }

        // 2) 주문 생성 (CREATED 상태)
        Order order = Order.create(userId, DEFAULT_STORE_ID, OrderType.CART);

        // 3) 각 장바구니 항목 → 재고 차감 + 주문아이템 (헬퍼 재사용)
        for (CartItem cartItem : cartItems) {
            Long optionId = cartItem.getClothesOption().getId();
            Integer quantity = cartItem.getQuantity();

            OrderItem orderItem = buildItemAndDecreaseStock(optionId, quantity);
            order.addItem(orderItem);  // 양방향 연관 + totalPrice 누적
        }

        // 4) mock 결제: CREATED → PAID + paid_at 기록
        order.markPaid(LocalDateTime.now());

        // 5) 저장 (OrderItem은 cascade로 함께 저장)
        Order saved = orderRepository.save(order);

        // 6) CART 주문 성공 → cart_items만 비우고 carts 행은 유지
        cart.getItems().clear();   // orphanRemoval=true 라 DELETE 실행

        return OrderResponse.from(saved);
    }

    @Override
    @Transactional
    public OrderResponse orderDirect(Long userId, DirectOrderRequest request) {
        // 1) 주문 생성 (CREATED 상태, 타입 DIRECT)
        Order order = Order.create(userId, DEFAULT_STORE_ID, OrderType.DIRECT);

        // 2) 단일 옵션 → 재고 차감 + 주문아이템 (헬퍼 재사용, 1회만)
        OrderItem orderItem = buildItemAndDecreaseStock(
                request.clothesOptionId(),
                request.quantity()
        );
        order.addItem(orderItem);

        // 3) mock 결제: CREATED → PAID + paid_at 기록
        order.markPaid(LocalDateTime.now());

        // 4) 저장 (OrderItem은 cascade로 함께)
        Order saved = orderRepository.save(order);

        return OrderResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(OrderErrorCode.ORDER_NOT_FOUND));

        // 본인 주문만 조회 가능
        if (!order.getUserId().equals(userId)) {
            throw new CustomException(OrderErrorCode.ORDER_ACCESS_DENIED);
        }

        return OrderResponse.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getMyOrders(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        Page<OrderSummaryResponse> mapped = orders.map(OrderSummaryResponse::from);
        return PageResponse.from(mapped);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(OrderErrorCode.ORDER_NOT_FOUND));

        // 1) 권한 검증: 본인 주문만 (CUSTOMER 기준)
        if (!order.getUserId().equals(userId)) {
            throw new CustomException(OrderErrorCode.ORDER_ACCESS_DENIED);
        }

        // 2) 취소 직전 상태 기억 (복구 여부 판단용)
        OrderStatus before = order.getStatus();

        // 3) 상태 전이: CREATED/PAID/PREPARING → CANCELED (그 외엔 엔티티가 예외)
        order.cancel();

        // 4) 재고 복구: 차감이 있었던 상태(PAID/PREPARING)에서만
        if (before == OrderStatus.PAID || before == OrderStatus.PREPARING) {
            for (OrderItem item : order.getItems()) {
                clothesOptionRepository.increaseStock(
                        item.getClothesOption().getId(),
                        item.getQuantity()
                );
            }
        }

        return OrderResponse.from(order);
    }
}
