package com.example.bridgelink.weatherinfo

import android.util.Log
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

class WeatherInfoRepository {

    private val apiKey = "IFAfVIKRKDrSRKD7Tr8sqEERlvX9GedS" // Replace with your AccuWeather API key
    private val client = OkHttpClient()

    suspend fun fetchLocationKey(location: String): String? = withContext(Dispatchers.IO) {
        Log.d("WeatherInfoRepository", "Fetching location key for $location")
        val url = "https://dataservice.accuweather.com/locations/v1/cities/search?q=$location&apikey=$apiKey"
        val request = Request.Builder()
            .url(url)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                json.getJSONArray("Key").getString(0) // Adjust based on actual response structure
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("WeatherInfoRepository", "Error fetching location key", e)
            null
        }
    }

    suspend fun fetchWeatherData(locationKey: String): WeatherInfo? = withContext(Dispatchers.IO) {
        val url = "https://dataservice.accuweather.com/currentconditions/v1/$locationKey?apikey=$apiKey"
        val request = Request.Builder()
            .url(url)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                // Assumed response structure, you may need to adjust this
                val weather = json.getJSONArray("CurrentConditions").getJSONObject(0)

                val temperature = weather.getJSONObject("Temperature")
                    .getJSONObject("Metric").getInt("Value")
                val condition = weather.getString("WeatherText")

                val description = "Temperature: $temperature°C, Condition: $condition" // Simplified
                WeatherInfo(temperature, condition, description)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("WeatherInfoRepository", "Error fetching weather data", e)
            null
        }
    }

}
