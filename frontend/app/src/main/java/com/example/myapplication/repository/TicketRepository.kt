package com.example.myapplication.repository

import com.example.myapplication.network.RetrofitClient

class TicketRepository {

    suspend fun getTickets() =
        RetrofitClient.api.getTickets()

    suspend fun getStats() =
        RetrofitClient.api.getMonthlyExpense()
}