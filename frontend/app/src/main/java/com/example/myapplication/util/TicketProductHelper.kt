package com.example.myapplication.util

import com.example.myapplication.model.ProductDto

object TicketProductHelper {

    fun prepareForSave(productos: List<ProductDto>, totalPrice: Double): List<ProductDto> {
        val valid = productos.filter { it.nombre.isNotBlank() }
        if (valid.isEmpty()) return emptyList()

        if (valid.size == 1) {
            return listOf(valid.first().copy(precio = valid.first().precio ?: totalPrice))
        }

        return valid.map { product ->
            product.copy(precio = product.precio ?: 0.0)
        }.filter { (it.precio ?: 0.0) > 0.0 }
    }
}
