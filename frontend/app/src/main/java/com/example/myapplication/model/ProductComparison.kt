package com.example.myapplication.model

data class StorePriceEntry(
    val store: String,
    val price: Double
)

data class ProductComparison(
    val productName: String,
    val entries: List<StorePriceEntry>
) {
    val minPrice: Double get() = entries.minOfOrNull { it.price } ?: 0.0
    val maxPrice: Double get() = entries.maxOfOrNull { it.price } ?: 0.0
    val priceDiff: Double get() = maxPrice - minPrice
    val hasDifference: Boolean get() = entries.size > 1 && priceDiff > 0.01
}
