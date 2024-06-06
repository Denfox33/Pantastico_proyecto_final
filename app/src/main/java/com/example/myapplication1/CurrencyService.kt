package com.example.myapplication1

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyService {
    @GET("latest")
    fun getLatestRates(@Query("base") base: String): Call<CurrencyResponse>
}