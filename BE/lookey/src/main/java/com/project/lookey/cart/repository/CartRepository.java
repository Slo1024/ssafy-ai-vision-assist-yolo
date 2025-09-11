package com.project.lookey.cart.repository;

import com.project.lookey.cart.entity.Cart;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    interface Row {
        Integer getCartId();
        Long getProductId();
        String getProductName();
    }

    @Query("""
        select
          c.id as cartId,
          p.id as productId,
          p.name as productName
        from Cart c
        join c.product p
        where c.user.id = :userId
        order by c.id desc
    """)
    List<Row> findRowsByUserId(@Param("userId") Integer userId);

    boolean existsByUser_IdAndProduct_Id(Integer userId, Long productId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Cart c where c.id = :cartId and c.user.id = :userId")
    int deleteByIdAndUserId(@Param("cartId") Integer cartId, @Param("userId") Integer userId);
}
