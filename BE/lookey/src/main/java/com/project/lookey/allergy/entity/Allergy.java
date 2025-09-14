package com.project.lookey.allergy.entity;

import com.project.lookey.OAuth.Entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "allergy",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UQ_ALLERGY_USER_ALLERGYLIST",
                        columnNames = {"user_id", "allergy_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Allergy {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "allergy_id", nullable = false)
    private AllergyList allergyList;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}