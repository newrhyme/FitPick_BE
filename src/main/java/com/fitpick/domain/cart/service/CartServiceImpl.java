package com.fitpick.domain.cart.service;

import com.fitpick.domain.cart.dto.CartItemAddRequest;
import com.fitpick.domain.cart.dto.CartItemQuantityUpdateRequest;
import com.fitpick.domain.cart.dto.CartResponse;
import com.fitpick.domain.cart.entity.Cart;
import com.fitpick.domain.cart.entity.CartItem;
import com.fitpick.domain.cart.exception.CartErrorCode;
import com.fitpick.domain.cart.repository.CartItemRepository;
import com.fitpick.domain.cart.repository.CartRepository;
import com.fitpick.domain.clothes.entity.ClothesOption;
import com.fitpick.domain.clothes.exception.ClothesErrorCode;
import com.fitpick.domain.clothes.repository.ClothesOptionRepository;
import com.fitpick.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private static final Long DEFAULT_STORE_ID = 1L;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ClothesOptionRepository clothesOptionRepository;

    @Override
    @Transactional
    public CartResponse addItem(Long userId, CartItemAddRequest request) {
        // 1) 담을 옵션 조회 (없으면 예외)
        ClothesOption option = clothesOptionRepository.findById(request.optionId())
                .orElseThrow(() -> new CustomException(ClothesErrorCode.CLOTHES_OPTION_NOT_FOUND));

        // 2) 내 장바구니 조회, 없으면 새로 생성
        Cart cart = cartRepository.findByUserIdAndStoreId(userId, DEFAULT_STORE_ID)
                .orElseGet(() -> cartRepository.save(Cart.create(userId, DEFAULT_STORE_ID)));

        // 3) 이미 같은 옵션 담겨있는지 확인
        CartItem existing = cartItemRepository
                .findByCartIdAndClothesOptionId(cart.getId(), option.getId())
                .orElse(null);

        // 4) 합산 후 최종 수량 계산
        int finalQuantity = (existing != null)
                ? existing.getQuantity() + request.quantity()
                : request.quantity();

        // 5) 재고 체크 (합산된 최종 수량 기준)
        if (option.getStockQuantity() < finalQuantity) {
            throw new CustomException(CartErrorCode.OUT_OF_STOCK);
        }

        // 6) 이미 있으면 수량 합산, 없으면 새 항목 추가
        if (existing != null) {
            existing.addQuantity(request.quantity());
        } else {
            cartItemRepository.save(CartItem.create(cart, option, request.quantity()));
        }

        // 7) 장바구니 return
        return getMyCart(userId);
    }

    @Override
    public CartResponse getMyCart(Long userId) {
        // 장바구니 없으면 빈 장바구니 응답
        Cart cart = cartRepository.findByUserIdAndStoreId(userId, DEFAULT_STORE_ID)
                .orElse(null);

        if (cart == null) {
            return CartResponse.empty();
        }

        // Repository로 직접 조회
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        return CartResponse.of(cart,items);
    }

    @Override
    @Transactional
    public CartResponse changeQuantity(Long userId, Long cartItemId, CartItemQuantityUpdateRequest request) {
        // 1) 수량 검증
        if (request.quantity() < 1) {
            throw new CustomException(CartErrorCode.INVALID_QUANTITY);
        }

        // 2) 항목 조회
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CustomException(CartErrorCode.CART_ITEM_NOT_FOUND));

        // 3) 본인 장바구니 항목인지 확인
        validateOwner(item, userId);

        // 4) 재고 체크
        if (item.getClothesOption().getStockQuantity() < request.quantity()) {
            throw new CustomException(CartErrorCode.OUT_OF_STOCK);
        }

        // 5) 수량 변경
        item.changeQuantity(request.quantity());

        return getMyCart(userId);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CustomException(CartErrorCode.CART_ITEM_NOT_FOUND));

        // 본인건지 확인
        validateOwner(item, userId);

        cartItemRepository.delete(item);

        return getMyCart(userId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        // 장바구니 없으면 이미 비어있는 상태와 동등 — 멱등 처리
        cartRepository.findByUserIdAndStoreId(userId, DEFAULT_STORE_ID)
                .ifPresent(cart -> cartItemRepository.deleteAllByCartId(cart.getId()));
    }

    // 항목이 현재 사용자의 장바구니 것인지 검증
    private void validateOwner(CartItem item, Long userId) {
        if (!item.getCart().getUserId().equals(userId)) {
            throw new CustomException(CartErrorCode.FORBIDDEN_CART_ACCESS);
        }
    }
}
