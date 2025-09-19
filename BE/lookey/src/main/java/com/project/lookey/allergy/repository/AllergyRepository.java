package com.project.lookey.allergy.repository;

import com.project.lookey.allergy.entity.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, Long> {

    @Query("""
        SELECT new com.project.lookey.allergy.dto.AllergyListResponse.Item(
            a.id,
            a.allergyList.id,
            a.allergyList.name
        )
        FROM Allergy a
        WHERE a.user.id = :userId
        ORDER BY a.createdAt DESC
    """)
    List<com.project.lookey.allergy.dto.AllergyListResponse.Item> findRowsByUserId(@Param("userId") Integer userId);

    boolean existsByUser_IdAndAllergyList_Id(Integer userId, Long allergyListId);

    @Modifying
    @Query("DELETE FROM Allergy a WHERE a.user.id = :userId AND a.allergyList.id = :allergyListId")
    int deleteByUserIdAndAllergyListId(@Param("userId") Integer userId, @Param("allergyListId") Long allergyListId);
}