package com.example.myapplication.model

data class CreateTicketRequest(
    val nombre: String,
    val precio: Double,
    val categoria: String,
    val productos: List<ProductDto> = emptyList()
)
