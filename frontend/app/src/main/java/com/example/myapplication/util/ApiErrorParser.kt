package com.example.myapplication.util

import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response

object ApiErrorParser {

    private val gson = Gson()

    fun message(response: Response<*>): String {
        val fallback = when (response.code()) {
            409 -> "El email ya está registrado"
            401 -> "Credenciales incorrectas"
            400 -> "Datos no válidos"
            404 -> "Recurso no encontrado"
            else -> "Error del servidor (${response.code()})"
        }

        return try {
            val body = response.errorBody()?.string().orEmpty()
            if (body.isBlank()) return fallback

            val json = gson.fromJson(body, JsonObject::class.java)
            json.get("error")?.asString?.takeIf { it.isNotBlank() }
                ?: json.get("message")?.asString?.takeIf { it.isNotBlank() }
                ?: fallback
        } catch (_: Exception) {
            fallback
        }
    }
}
