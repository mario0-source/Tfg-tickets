package com.example.myapplication.network

import com.example.myapplication.model.CreateTicketRequest
import com.example.myapplication.model.LoginRequest
import com.example.myapplication.model.LoginResponse
import com.example.myapplication.model.MonthlyExpenseDto
import com.example.myapplication.model.TicketDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("api/login_check")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>
    @POST("api/register")
    fun register(
        @Body request: LoginRequest
    ): Call<Void>
    @GET("api/tickets")
    suspend fun getTickets(): List<TicketDto>

    @POST("api/tickets")
    fun createTicket(
        @Body request: CreateTicketRequest
    ): Call<Map<String, Any>>

    @GET("api/stats/monthly-expense")
    suspend fun getMonthlyExpense(): MonthlyExpenseDto


}