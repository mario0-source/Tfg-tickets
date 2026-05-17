package com.example.myapplication.model

data class MonthlyExpenseDto(
    val total: Double,
    val variationPercent: Double,
    val ticketsCount: Int,
    val categoriesCount: Int
)