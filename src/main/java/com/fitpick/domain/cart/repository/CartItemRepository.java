package com.fitpick.domain.cart.repository;

import com.fitpick.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 장바구니에 같은 옵션 이미 있는지
    Optional<CartItem> findByCartIdAndClothesOptionId(Long cartId, Long clothesOptionId);

    List<CartItem> findByCartId(Long cartId);

    // 장바구니 주문 성공 후 호출. cart 엔티티가 detach 상태여도 안전한 bulk delete.
    @Modifying(clearAutomatically = true)
    @Query("delete from CartItem ci where ci.cart.id = :cartId")
    int deleteAllByCartId(@Param("cartId") Long cartId);
}
