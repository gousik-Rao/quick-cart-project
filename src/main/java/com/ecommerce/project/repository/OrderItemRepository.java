package com.ecommerce.project.repository;

import com.ecommerce.project.model.OrderItem;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM OrderItem oi WHERE oi.product.productId = :productId")
    void deleteAllByProductId(@Param("productId") Long productId);
}
