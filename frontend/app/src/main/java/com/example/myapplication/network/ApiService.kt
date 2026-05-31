package com.example.myapplication.network

import com.example.myapplication.model.CompareProductDetail
import com.example.myapplication.model.CompareProductSummary
import com.example.myapplication.model.CreateTicketRequest
import com.example.myapplication.model.LoginRequest
import com.example.myapplication.model.LoginResponse
import com.example.myapplication.model.ManualCompareEntryRequest
import com.example.myapplication.model.MonthlyExpenseDto
import com.example.myapplication.model.ProfileDto
import com.example.myapplication.model.TicketDto
import com.example.myapplication.model.UpdateTicketRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("api/login_check")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/register")
    fun register(@Body request: LoginRequest): Call<Void>

    @GET("api/tickets")
    suspend fun getTickets(): List<TicketDto>

    @GET("api/tickets/{id}")
    suspend fun getTicketById(@Path("id") id: Int): TicketDto

    @POST("api/tickets")
    fun createTicket(@Body request: CreateTicketRequest): Call<Map<String, Any>>

    @PUT("api/tickets/{id}")
    fun updateTicket(
        @Path("id") id: Int,
        @Body request: UpdateTicketRequest
    ): Call<Map<String, Any>>

    @DELETE("api/tickets/{id}")
    suspend fun deleteTicket(@Path("id") id: Int): Map<String, Any>

    @GET("api/stats/monthly-expense")
    suspend fun getMonthlyExpense(): MonthlyExpenseDto

    @GET("api/profile")
    suspend fun getProfile(): ProfileDto

    @GET("api/compare/products")
    suspend fun getCompareProducts(): List<CompareProductSummary>

    @GET("api/compare/product/{productName}")
    suspend fun getCompareProductDetail(
        @Path(value = "productName", encoded = true) productName: String
    ): CompareProductDetail

    @POST("api/compare/entries")
    suspend fun addManualCompareEntry(@Body request: ManualCompareEntryRequest): Map<String, Any>
}
