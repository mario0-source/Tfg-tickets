package com.example.myapplication.util

import com.example.myapplication.model.ProductDto
import java.text.SimpleDateFormat
import java.util.Locale

object FormValidators {

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "El email es obligatorio"
        !emailRegex.matches(email.trim()) -> "Formato de email no válido"
        else -> null
    }

    fun validatePassword(password: String, minLength: Int = 6): String? = when {
        password.isBlank() -> "La contraseña es obligatoria"
        password.length < minLength -> "Mínimo $minLength caracteres"
        else -> null
    }

    fun validateRequired(value: String, fieldLabel: String): String? =
        if (value.isBlank()) "$fieldLabel es obligatorio" else null

    fun validatePrice(text: String, required: Boolean = true): String? {
        if (text.isBlank()) {
            return if (required) "El precio es obligatorio" else null
        }

        val value = text.replace(",", ".").toDoubleOrNull()
        return when {
            value == null -> "Introduce un número válido"
            value < 0 -> "No puede ser negativo"
            value <= 0 -> "Debe ser mayor que 0"
            else -> null
        }
    }

    fun validateOptionalPrice(text: String): String? {
        if (text.isBlank()) return null
        val value = text.replace(",", ".").toDoubleOrNull()
        return when {
            value == null -> "Introduce un número válido"
            value < 0 -> "No puede ser negativo"
            value <= 0 -> "Debe ser mayor que 0"
            else -> null
        }
    }

    fun validateDate(text: String, required: Boolean = false): String? {
        if (text.isBlank()) {
            return if (required) "La fecha es obligatoria" else null
        }
        return if (parseDate(text) != null) null else "Usa el formato dd/mm/aaaa"
    }

    fun parseDate(text: String): String? {
        val patterns = listOf("dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss")
        val trimmed = text.trim()

        for (pattern in patterns) {
            try {
                val input = SimpleDateFormat(pattern, Locale.getDefault())
                input.isLenient = false
                val date = input.parse(trimmed) ?: continue
                return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
            } catch (_: Exception) {
                continue
            }
        }
        return null
    }

    fun validateProductPrices(productos: List<ProductDto>, singleProductMode: Boolean): Map<Int, String> {
        if (singleProductMode) return emptyMap()

        val errors = mutableMapOf<Int, String>()
        productos.forEachIndexed { index, product ->
            if (product.nombre.isBlank()) return@forEachIndexed

            when {
                product.precio == null -> errors[index] = "Precio obligatorio"
                product.precio < 0 -> errors[index] = "No puede ser negativo"
                product.precio <= 0 -> errors[index] = "Debe ser mayor que 0"
            }
        }
        return errors
    }

    fun hasAnyProductName(productos: List<ProductDto>): Boolean =
        productos.any { it.nombre.isNotBlank() }
}
