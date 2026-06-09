package com.example.myapplication.ui.compare

import androidx.compose.ui.graphics.Color
import com.example.myapplication.ui.theme.NebulaGreen
import kotlin.math.abs

object CompareStoreColors {

    val palette = listOf(
        NebulaGreen,
        Color(0xFF1A8CFF),
        Color(0xFFB84DFF),
        Color(0xFFFFB020),
        Color(0xFFFF6B8A),
        Color(0xFF40E0D0),
        Color(0xFFFF8C42),
        Color(0xFF9AE66E)
    )

    fun colorForStore(store: String): Color {
        val index = abs(store.lowercase().hashCode()) % palette.size
        return palette[index]
    }

    fun colorsByStore(stores: List<String>): Map<String, Color> =
        stores.distinct().associateWith { colorForStore(it) }
}
