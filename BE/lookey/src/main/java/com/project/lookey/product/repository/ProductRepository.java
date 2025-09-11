package com.project.lookey.product.repository;

import com.project.lookey.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByNameAndBrand(String name, String brand);

    interface NameView {
        Long getId();
        String getName();
    }

    @Query("""
        select p.id as id, p.name as name
        from Product p
        where p.name like concat('%', :q, '%')
        order by p.name asc
    """)
    List<NameView> findNamesByKeyword(@Param("q") String q);
}
