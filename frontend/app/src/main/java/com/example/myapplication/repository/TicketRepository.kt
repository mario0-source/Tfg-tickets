package com.example.myapplication.repository

import com.example.myapplication.model.ManualCompareEntryRequest
import com.example.myapplication.model.UpdateTicketRequest
import com.example.myapplication.network.RetrofitClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class TicketRepository {

    suspend fun getTickets() = RetrofitClient.api.getTickets()

    suspend fun getStats() = RetrofitClient.api.getMonthlyExpense()

    suspend fun getProfile() = RetrofitClient.api.getProfile()

    suspend fun getTicketById(id: Int) = RetrofitClient.api.getTicketById(id)

    suspend fun getCompareProducts() = RetrofitClient.api.getCompareProducts()

    suspend fun getCompareProductDetail(productName: String) =
        RetrofitClient.api.getCompareProductDetail(
            URLEncoder.encode(productName, StandardCharsets.UTF_8.toString())
        )

    suspend fun addManualCompareEntry(productName: String, store: String, price: Double) =
        RetrofitClient.api.addManualCompareEntry(
            ManualCompareEntryRequest(
                productName = productName,
                store = store,
                price = price
            )
        )

    fun updateTicket(id: Int, request: UpdateTicketRequest) =
        RetrofitClient.api.updateTicket(id, request)

    suspend fun deleteTicket(id: Int) = RetrofitClient.api.deleteTicket(id)
}
