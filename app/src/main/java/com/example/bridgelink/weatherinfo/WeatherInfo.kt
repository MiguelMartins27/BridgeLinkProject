package com.example.bridgelink.weatherinfo

data class WeatherInfo(
    val temperature: Int,
    val condition: String,
    val description: String,
    val iconResource: Int
)
