package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

data class ProfileDto(
    @SerializedName("email")
    val email: String,
    @SerializedName("ticketsCount")
    val ticketsCount: Int,
    @SerializedName("totalSpent")
    val totalSpent: Double,
    @SerializedName("avgSpendPerTicket")
    val avgSpendPerTicket: Double = 0.0,
    @SerializedName("topCategory")
    val topCategory: String = "General",
    @SerializedName("monthlySpent")
    val monthlySpent: Double = 0.0
)
