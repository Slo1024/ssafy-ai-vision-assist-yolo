package com.project.lookey.allergy.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "allergy_list")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AllergyList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;
}
