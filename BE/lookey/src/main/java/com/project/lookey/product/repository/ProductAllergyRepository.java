package com.project.lookey.product.repository;

import com.project.lookey.allergy.entity.Allergy;
import com.project.lookey.allergy.entity.AllergyList;
import com.project.lookey.product.entity.Product;
import com.project.lookey.product.entity.ProductAllergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAllergyRepository extends JpaRepository<ProductAllergy, Long> {
    boolean existsByProductAndAllergy_Name(Product product, String name);

    boolean existsByProductAndAllergy(Product product, AllergyList allergy);

    List<ProductAllergy> findByProduct(Product product);

}

