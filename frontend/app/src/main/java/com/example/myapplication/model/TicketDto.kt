package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

data class TicketDto(

    @SerializedName("id")
    val id: Int,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("precio")
    val precio: Double,

    @SerializedName("categoria")
    val categoria: String,

    @SerializedName("fecha")
    val fecha: String
)