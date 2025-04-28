package com.example.beadando.model

data class FoodResponse(
    val count: Int,
    val products: List<Product>
)

data class Product(
    val product_name: String?,
    val nutriments: Nutriments?
)

data class Nutriments(
    val energy: Double?,
    val proteins: Double?,
    val carbohydrates: Double?,
    val fat: Double?
)
