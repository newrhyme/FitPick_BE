package com.fitpick.domain.tryon.repository;

import com.fitpick.domain.tryon.entity.TryOn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TryOnRepository extends JpaRepository<TryOn, Long> {

    @Query("SELECT DISTINCT t FROM TryOn t LEFT JOIN FETCH t.items WHERE t.id = :id")
    Optional<TryOn> findByIdWithItems(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT t FROM TryOn t
            LEFT JOIN FETCH t.items
            WHERE t.userId = :userId
            ORDER BY t.createdAt DESC
            """)
    List<TryOn> findAllByUserIdWithItemsOrderByCreatedAtDesc(@Param("userId") Long userId);
}
