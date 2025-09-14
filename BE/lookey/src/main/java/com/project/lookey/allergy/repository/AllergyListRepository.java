package com.project.lookey.allergy.repository;

import com.project.lookey.allergy.entity.AllergyList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllergyListRepository extends JpaRepository<AllergyList, Long> {

    @Query("""
        SELECT new com.project.lookey.allergy.dto.AllergySearchResponse$Item(
            al.id, 
            al.name
        )
        FROM AllergyList al 
        WHERE al.name LIKE %:keyword%
        ORDER BY al.name
    """)
    List<com.project.lookey.allergy.dto.AllergySearchResponse.Item> findNamesByKeyword(@Param("keyword") String keyword);
}