package com.example.myapplication.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://192.168.1.11:8000/"
    //10.0.2.2
    private var jwtToken: String? = null

    fun setToken(token: String?) {
        jwtToken = token
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(
            AuthInterceptor { jwtToken }
        )
        .build()

    val api: ApiService by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(ApiService::class.java)
    }
}