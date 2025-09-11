package com.project.lookey.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "product",
        uniqueConstraints = @UniqueConstraint(name="uk_product_name_brand",
                columnNames = {"name","brand"}))
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=255)
    private String name;

    @Column(nullable=false)
    private Integer price;

    @Column(length=100)
    private String event;

    @Column(length=20)
    private String brand;

    @CreationTimestamp
    private LocalDateTime created_at;

    @UpdateTimestamp
    private LocalDateTime updated_at;
}