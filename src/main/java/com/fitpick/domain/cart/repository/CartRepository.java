package com.fitpick.domain.cart.repository;

import com.fitpick.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // 내 장바구니 조회 (user + store 조합 -> 1개)
    Optional<Cart> findByUserIdAndStoreId(Long userId, Long storeId);
}
