package com.fitpick.domain.cart.repository;

import com.fitpick.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 장바구니에 같은 옵션 이미 있는지
    Optional<CartItem> findByCartIdAndClothesOptionId(Long cartId, Long clothesOptionId);

    List<CartItem> findByCartId(Long cartId);
}
