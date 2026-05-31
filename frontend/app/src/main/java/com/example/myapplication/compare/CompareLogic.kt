package com.example.myapplication.compare

import com.example.myapplication.model.ProductComparison
import com.example.myapplication.model.StorePriceEntry
import com.example.myapplication.model.TicketDto
import java.text.Normalizer

object CompareLogic {

    fun buildComparisons(tickets: List<TicketDto>): List<ProductComparison> {
        val grouped = mutableMapOf<String, MutableList<StorePriceEntry>>()
        val displayNames = mutableMapOf<String, String>()

        tickets.forEach { ticket ->
            val ticketProducts = resolveProducts(ticket)

            ticketProducts.forEach { product ->
                val key = normalizeProductName(product.nombre)
                if (key.isBlank()) return@forEach

                val price = product.precio ?: 0.0
                if (price <= 0.0) return@forEach

                displayNames.putIfAbsent(key, product.nombre.trim())
                grouped.getOrPut(key) { mutableListOf() }.add(
                    StorePriceEntry(
                        store = ticket.nombre,
                        price = price
                    )
                )
            }
        }

        return grouped.map { (key, entries) ->
            ProductComparison(
                productName = displayNames[key] ?: key,
                entries = entries
                    .groupBy { it.store.lowercase() }
                    .map { (_, storeEntries) -> storeEntries.minBy { it.price } }
                    .sortedBy { it.price }
            )
        }
            .filter { it.entries.isNotEmpty() }
            .sortedWith(
                compareByDescending<ProductComparison> { it.hasDifference }
                    .thenBy { it.productName.lowercase() }
            )
    }

    private fun resolveProducts(ticket: TicketDto): List<com.example.myapplication.model.ProductDto> {
        if (ticket.productos.isNotEmpty()) return ticket.productos

        return emptyList()
    }

    private fun normalizeProductName(name: String): String {
        return Normalizer.normalize(name.trim().lowercase(), Normalizer.Form.NFD)
            .replace(Regex("""\p{M}"""), "")
            .replace(Regex("""[^a-z0-9\s]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }
}
