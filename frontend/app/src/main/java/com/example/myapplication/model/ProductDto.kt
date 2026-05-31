package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("precio")
    val precio: Double? = null
)
