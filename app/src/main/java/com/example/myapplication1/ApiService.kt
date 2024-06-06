package com.example.myapplication1


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.myapplication1.CurrencyService

object ApiService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.exchangeratesapi.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val currencyService: CurrencyService = retrofit.create(CurrencyService::class.java)
}