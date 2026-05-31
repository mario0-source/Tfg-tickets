package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

data class CompareProductSummary(
    @SerializedName("productName")
    val productName: String,
    @SerializedName("storeCount")
    val storeCount: Int,
    @SerializedName("minPrice")
    val minPrice: Double,
    @SerializedName("maxPrice")
    val maxPrice: Double,
    @SerializedName("priceDiff")
    val priceDiff: Double
)

data class ComparePriceEntry(
    @SerializedName("store")
    val store: String,
    @SerializedName("price")
    val price: Double,
    @SerializedName("source")
    val source: String,
    @SerializedName("date")
    val date: String?
)

data class CompareProductDetail(
    @SerializedName("productName")
    val productName: String,
    @SerializedName("entries")
    val entries: List<ComparePriceEntry>,
    @SerializedName("minPrice")
    val minPrice: Double,
    @SerializedName("maxPrice")
    val maxPrice: Double,
    @SerializedName("priceDiff")
    val priceDiff: Double
)

data class ManualCompareEntryRequest(
    @SerializedName("productName")
    val productName: String,
    @SerializedName("store")
    val store: String,
    @SerializedName("price")
    val price: Double
)

data class UpdateTicketRequest(
    val nombre: String,
    val precio: Double,
    val categoria: String,
    val productos: List<ProductDto> = emptyList()
)
