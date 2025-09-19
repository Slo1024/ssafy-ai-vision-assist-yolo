package com.project.lookey.allergy.service;

import com.project.lookey.OAuth.Repository.UserRepository;
import com.project.lookey.allergy.dto.AllergyAddRequest;
import com.project.lookey.allergy.dto.AllergyListResponse;
import com.project.lookey.allergy.dto.AllergyRemoveRequest;
import com.project.lookey.allergy.dto.AllergySearchResponse;
import com.project.lookey.allergy.entity.Allergy;
import com.project.lookey.allergy.repository.AllergyListRepository;
import com.project.lookey.allergy.repository.AllergyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AllergyService {

    private final AllergyRepository allergyRepository;
    private final AllergyListRepository allergyListRepository;
    private final UserRepository userRepository;

    public AllergyListResponse getMyAllergies(Integer userId) {
        var allergies = allergyRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        var items = allergies.stream()
                .map(allergy -> new com.project.lookey.allergy.dto.AllergyListResponse.Item(
                        allergy.getId(),
                        allergy.getAllergyList().getId(),
                        allergy.getAllergyList().getName()
                ))
                .collect(java.util.stream.Collectors.toList());
        return new AllergyListResponse(items);
    }

    public AllergySearchResponse searchAllergies(String keyword) {
        if (keyword == null || keyword.isBlank()) return new AllergySearchResponse(java.util.List.of());
        var allergyLists = allergyListRepository.findByNameContainingOrderByName(keyword.trim());
        var items = allergyLists.stream()
                .map(al -> new com.project.lookey.allergy.dto.AllergySearchItem(al.getId(), al.getName()))
                .collect(java.util.stream.Collectors.toList());
        return new AllergySearchResponse(items);
    }

    @Transactional
    public void addAllergy(Integer userId, AllergyAddRequest req) {
        Long allergyListId = req.allergyId();

        var allergyList = allergyListRepository.findById(allergyListId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "알레르기를 찾을 수 없습니다."));

        var userRef = userRepository.getReferenceById(userId);

        if (allergyRepository.existsByUser_IdAndAllergyList_Id(userId, allergyListId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 알레르기입니다.");
        }

        try {
            allergyRepository.save(Allergy.builder().user(userRef).allergyList(allergyList).build());
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 알레르기입니다.");
        }
    }

    @Transactional
    public void removeAllergy(Integer userId, AllergyRemoveRequest req) {
        int affected = allergyRepository.deleteByUser_IdAndAllergyList_Id(userId, req.allergyId());
        if (affected == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "알레르기 항목을 찾을 수 없습니다.");
        }
    }
}