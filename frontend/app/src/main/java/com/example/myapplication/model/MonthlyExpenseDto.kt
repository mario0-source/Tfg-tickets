package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

data class MonthlyExpenseDto(
    @SerializedName("total")
    val total: Double,

    @SerializedName("variationPercent")
    val variationPercent: Double,

    @SerializedName("ticketsCount")
    val ticketsCount: Int,

    @SerializedName("categoriesCount")
    val categoriesCount: Int
)