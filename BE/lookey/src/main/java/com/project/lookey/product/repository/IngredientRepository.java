package com.project.lookey.product.repository;

import com.project.lookey.product.entity.Ingredient;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    @Transactional
    @Modifying
    @Query("delete from Ingredient i where i.product_id = :pid")
    void deleteByProductId(@Param("pid") Long productId);
}
