package com.example.bridgelink.weatherinfo

data class WeatherInfo(
    val temperature: Int,
    val condition: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val iconResourceId: Int
)
