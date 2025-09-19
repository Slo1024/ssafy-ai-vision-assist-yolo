package com.project.lookey.allergy.repository;

import com.project.lookey.allergy.entity.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, Long> {

    List<Allergy> findByUser_IdOrderByCreatedAtDesc(Integer userId);

    boolean existsByUser_IdAndAllergyList_Id(Integer userId, Long allergyListId);

    int deleteByUser_IdAndAllergyList_Id(Integer userId, Long allergyListId);
}