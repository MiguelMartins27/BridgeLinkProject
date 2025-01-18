package com.example.bridgelink.weatherinfo

import android.util.Log
import com.example.bridgelink.R
import com.example.bridgelink.signals.Signal
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class WeatherInfoRepository {

    private val apiKey = "IFAfVIKRKDrSRKD7Tr8sqEERlvX9GedS" // Replace with your AccuWeather API key
    private val client = OkHttpClient()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("weather")

    suspend fun fetchLocationKey(location: String): String? = withContext(Dispatchers.IO) {
        Log.d("WeatherInfoRepository", "Fetching location key for $location")
        val url = "https://dataservice.accuweather.com/locations/v1/cities/search?q=$location&apikey=$apiKey"
        val request = Request.Builder()
            .url(url)
            .build()
        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            Log.d("WeatherInfoRepository", "Response: $responseBody")
            if (response.isSuccessful && responseBody != null) {
                val jsonArray = JSONArray(responseBody)
                getLocationKey(jsonArray, location)
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
                val jsonArray = JSONArray(responseBody) // The response should be an array, not a JSONObject
                val weather = jsonArray.getJSONObject(0) // Assuming the first item in the array contains weather data

                // Accessing temperature and condition from the response
                val temperature = weather.getJSONObject("Temperature")
                    .getJSONObject("Metric").getInt("Value") // Assuming the "Metric" contains the value

                val condition = weather.getString("WeatherText") // The weather description

                // Constructing a simple weather description
                val description = "Temperature: $temperature°C, Condition: $condition"
                WeatherInfo(temperature, condition, description, getDrawableForWeather(condition))
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("WeatherInfoRepository", "Error fetching weather data", e)
            null
        }
    }

    private fun getLocationKey(jsonArray: JSONArray, location: String): String? {
        for (i in 0 until jsonArray.length()) {
            val locationData = jsonArray.getJSONObject(i)
            if (locationData.getString("LocalizedName") == location) {
                return locationData.getString("Key")
            }
        }
        return null
    }

    fun fetchWeather(onDataFetched: (List<WeatherInfo>) -> Unit) {
        val weatherList = mutableListOf<WeatherInfo>()

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                weatherList.clear()

                for (weatherSnapshot in snapshot.children) {


                    // Validate iconResourceName
                    weatherList.add(WeatherInfo(temperature, condition, description, getDrawableForWeather(condition)))
                }

                onDataFetched(weatherList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun getDrawableForWeather(weather: String): Int {
        return when (weather) {
            "Sunny" -> R.drawable.w01
            "Mostly Sunny" -> R.drawable.w02
            "Partly Sunny" -> R.drawable.w03
            "Intermittent Clouds" -> R.drawable.w04
            "Hazy Sunshine" -> R.drawable.w05
            "Mostly Cloudy" -> R.drawable.w06
            "Cloudy" -> R.drawable.w07
            "Dreary (Overcast)" -> R.drawable.w08
            "Fog" -> R.drawable.w11
            "Showers" -> R.drawable.w12
            "Mostly Cloudy w/ Showers" -> R.drawable.w13
            "Partly Sunny w/ Showers" -> R.drawable.w14
            "T-Storms" -> R.drawable.w15
            "Mostly Cloudy w/ T-Storms" -> R.drawable.w16
            "Partly Sunny w/ T-Storms" -> R.drawable.w17
            "Rain" -> R.drawable.w18
            "Light Rain" -> R.drawable.w18
            "Flurries" -> R.drawable.w19
            "Mostly Cloudy w/ Flurries" -> R.drawable.w20
            "Partly Sunny w/ Flurries" -> R.drawable.w21
            "Snow" -> R.drawable.w22
            "Mostly Cloudy w/ Snow" -> R.drawable.w23
            "Ice" -> R.drawable.w24
            "Sleet" -> R.drawable.w25
            "Freezing Rain" -> R.drawable.w26
            "Rain and Snow" -> R.drawable.w29
            "Windy" -> R.drawable.w32
            "Clear" -> R.drawable.w33
            "Mostly Clear" -> R.drawable.w34
            "Partly Cloudy" -> R.drawable.w35
            "Hazy Moonlight" -> R.drawable.w37
            "Partly Cloudy w/ Showers" -> R.drawable.w39
            "Partly Cloudy w/ T-Storms" -> R.drawable.w41
            else -> R.drawable.unknown
        }
    }


}
