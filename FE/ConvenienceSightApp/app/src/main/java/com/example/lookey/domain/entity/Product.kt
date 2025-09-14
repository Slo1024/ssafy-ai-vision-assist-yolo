package com.example.lookey.domain.entity

data class Product(
    val id: String,
    val name: String,
    val volumeMl: Int? = null,
    val imageUrl: String? = null
)
