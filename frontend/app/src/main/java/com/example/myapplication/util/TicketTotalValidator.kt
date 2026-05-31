package com.example.myapplication.util

import com.example.myapplication.model.ProductDto
import kotlin.math.abs

sealed class TicketValidationState {
    data object Idle : TicketValidationState()
    data object Valid : TicketValidationState()
    data class SuggestAdjust(
        val difference: Double,
        val productsSum: Double,
        val total: Double
    ) : TicketValidationState()

    data class Invalid(
        val difference: Double,
        val productsSum: Double,
        val total: Double
    ) : TicketValidationState()
}

object TicketTotalValidator {

    private const val TOLERANCE = 0.01
    private const val AUTO_ADJUST_THRESHOLD = 0.5

    fun validate(total: Double, productos: List<ProductDto>, singleProductMode: Boolean): TicketValidationState {
        val validProducts = productos.filter { it.nombre.isNotBlank() }

        if (total <= 0.0 || validProducts.isEmpty()) {
            return TicketValidationState.Idle
        }

        if (singleProductMode) {
            return TicketValidationState.Valid
        }

        val sum = validProducts.sumOf { it.precio ?: 0.0 }
        val difference = abs(total - sum)

        return when {
            difference <= TOLERANCE -> TicketValidationState.Valid
            difference < AUTO_ADJUST_THRESHOLD -> TicketValidationState.SuggestAdjust(difference, sum, total)
            else -> TicketValidationState.Invalid(difference, sum, total)
        }
    }

    fun autoAdjustProducts(productos: List<ProductDto>, total: Double): List<ProductDto> {
        val valid = productos.filter { it.nombre.isNotBlank() }.toMutableList()
        if (valid.isEmpty()) return valid

        if (valid.size == 1) {
            valid[0] = valid[0].copy(precio = total)
            return valid
        }

        val sum = valid.sumOf { it.precio ?: 0.0 }
        val diff = total - sum
        val lastIndex = valid.lastIndex
        valid[lastIndex] = valid[lastIndex].copy(
            precio = (valid[lastIndex].precio ?: 0.0) + diff
        )
        return valid
    }

    fun canSave(state: TicketValidationState): Boolean {
        return state is TicketValidationState.Valid ||
                state is TicketValidationState.SuggestAdjust ||
                state is TicketValidationState.Idle
    }

    fun mustBlockSave(state: TicketValidationState): Boolean {
        return state is TicketValidationState.Invalid
    }
}
