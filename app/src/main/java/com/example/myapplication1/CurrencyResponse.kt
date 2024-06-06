package com.example.myapplication1

data class CurrencyResponse(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)