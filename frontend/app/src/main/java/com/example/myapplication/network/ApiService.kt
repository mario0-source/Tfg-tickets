package com.example.myapplication.network

import com.example.myapplication.model.LoginRequest
import com.example.myapplication.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/login")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @POST("api/register")
    fun register(
        @Body request: LoginRequest
    ): Call<Void>
}