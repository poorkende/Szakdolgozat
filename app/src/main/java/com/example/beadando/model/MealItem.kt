package com.example.beadando.model

import java.io.Serializable

data class MealItem(
    val productName: String,
    val portionSize: Double,
    val calories: Double,
    val proteins: Double,
    val carbs: Double,
    val fats: Double
) : Serializable

