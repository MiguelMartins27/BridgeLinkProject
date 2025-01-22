package com.example.bridgelink.weatherinfo

import android.util.Log
import com.example.bridgelink.R
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

    suspend fun fetchWeatherData(locationKey: String, latitude: Double, longitude: Double): WeatherInfo? = withContext(Dispatchers.IO) {
        val url = "https://dataservice.accuweather.com/currentconditions/v1/$locationKey?apikey=$apiKey"
        val request = Request.Builder()
            .url(url)
            .build()
        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val jsonArray = JSONArray(responseBody)
                val weather = jsonArray.getJSONObject(0)

                val temperature = weather.getJSONObject("Temperature")
                    .getJSONObject("Metric").getInt("Value")

                val condition = weather.getString("WeatherText")

                // Create WeatherInfo object
                val weatherInfo = WeatherInfo(temperature, condition, latitude, longitude, getDrawableForWeather(condition))

                // Save to Firebase on the IO dispatcher
                //withContext(Dispatchers.Main) {
                //    saveWeatherToFirebase(weatherInfo, locationKey)
                //}
//
                weatherInfo
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

    fun fetchWeather(onDataFetched: (List<WeatherInfo>) -> Unit, location: String, latitude: Double, longitude: Double) {
        val weatherList = mutableListOf<WeatherInfo>()
        Log.d("WeatherInfoRepository", "Fetching weather data for $location")
        database.child(location).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                weatherList.clear()
                val metricSnapshot = snapshot.child("Temperature").child("Metric")

                val value = metricSnapshot.child("Value").getValue(Int::class.java) ?: 0
                val condition = snapshot.child("WeatherText").getValue(String::class.java) ?: ""
                Log.d("WeatherInfoRepository", "Weather data: $value, $condition")

                weatherList.add(WeatherInfo(value, condition, latitude, longitude, getDrawableForWeather(condition)))
                saveWeatherToFirebase(WeatherInfo(value, condition, latitude, longitude, getDrawableForWeather(condition)), location)
                onDataFetched(weatherList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun saveWeatherToFirebase(weatherInfo: WeatherInfo, location: String) {
        val weatherRef = FirebaseDatabase.getInstance().reference
            .child("weatherCurrentInApp")
            .child(location)  // Add location as a child node

        val weatherData = hashMapOf(
            "temperature" to weatherInfo.temperature,
            "condition" to weatherInfo.condition,
            "latitude" to weatherInfo.latitude,
            "longitude" to weatherInfo.longitude,
            "iconResourceId" to weatherInfo.iconResourceId
        )

        weatherRef.setValue(weatherData)
            .addOnSuccessListener {
                Log.d("WeatherInfoRepository", "Weather data saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("WeatherInfoRepository", "Error saving weather data", e)
            }
    }



    private fun getDrawableForWeather(weather: String): Int {
        return when (weather) {
            "Sunny" -> R.drawable.w01
            "Mostly sunny" -> R.drawable.w02
            "Partly sunny" -> R.drawable.w03
            "Intermittent clouds" -> R.drawable.w04
            "Hazy sunshine" -> R.drawable.w05
            "Mostly cloudy" -> R.drawable.w06
            "Cloudy" -> R.drawable.w07
            "Dreary (Overcast)" -> R.drawable.w08
            "Fog" -> R.drawable.w11
            "Showers" -> R.drawable.w12
            "Mostly cloudy w/ showers" -> R.drawable.w13
            "Partly sunny w/ showers" -> R.drawable.w14
            "T-Storms" -> R.drawable.w15
            "Mostly cloudy w/ T-Storms" -> R.drawable.w16
            "Partly sunny w/ T-Storms" -> R.drawable.w17
            "Rain" -> R.drawable.w18
            "Light rain" -> R.drawable.w18
            "Flurries" -> R.drawable.w19
            "Mostly cloudy w/ flurries" -> R.drawable.w20
            "Partly sunny w/ flurries" -> R.drawable.w21
            "Snow" -> R.drawable.w22
            "Mostly cloudy w/ snow" -> R.drawable.w23
            "Ice" -> R.drawable.w24
            "Sleet" -> R.drawable.w25
            "Freezing rain" -> R.drawable.w26
            "Rain and snow" -> R.drawable.w29
            "Windy" -> R.drawable.w32
            "Clear" -> R.drawable.w33
            "Mostly clear" -> R.drawable.w34
            "Partly cloudy" -> R.drawable.w35
            "Hazy moonlight" -> R.drawable.w37
            "Partly cloudy w/ showers" -> R.drawable.w39
            "Partly cloudy w/ T-Storms" -> R.drawable.w41
            else -> R.drawable.unknown
        }
    }

}
