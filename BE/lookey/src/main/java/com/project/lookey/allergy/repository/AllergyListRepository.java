package com.project.lookey.allergy.repository;

import com.project.lookey.allergy.entity.AllergyList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AllergyListRepository extends JpaRepository<AllergyList, Long> {

    List<AllergyList> findByNameContainingOrderByName(String keyword);

    Optional<AllergyList> findByName(String name);
}