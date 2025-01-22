package com.example.bridgelink.danger.area

import android.util.Log
import com.example.bridgelink.weatherinfo.WeatherInfoRepository
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DangerAreaRepository(private val weatherRepository: WeatherInfoRepository) {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("danger_areas")

    suspend fun createDangerAreaIfRainingOnline(name: String, latitude: Double, longitude: Double, radius: Double) {
        // Fetch location key (you can use a hardcoded location or dynamic location)
        val location = "YourLocation"  // Example: Replace with dynamic location
        weatherRepository.fetchLocationKey(location)?.let { locationKey ->
            weatherRepository.fetchWeatherData(locationKey, latitude, longitude)?.let { weatherInfo ->
                if (weatherInfo.condition.contains("rain", ignoreCase = true) || weatherInfo.condition.contains("showers", ignoreCase = true)) {
                    // Proceed to create danger area if it’s raining
                    saveDangerArea(DangerArea(name, latitude, longitude, radius))
                } else {
                    // Optionally, notify the user that danger areas can only be created when it’s raining
                    Log.d("DangerAreaRepository", "Cannot create danger area: It's not raining.")
                }
            }
        }
    }

    fun createDangerAreaIfRainingLocal(
        location: String,
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Double
    ) {
        // Fetch weather for the specified location
        weatherRepository.fetchWeather({ weatherList ->
            if (weatherList.isNotEmpty()) {
                val weatherInfo = weatherList.first()
                // Check if the weather condition includes rain
                if (weatherInfo.condition.contains("rain", ignoreCase = true) ||
                    weatherInfo.condition.contains("showers", ignoreCase = true)
                ) {
                    // Proceed to create the danger area
                    saveDangerArea(DangerArea(name, latitude, longitude, radius))
                } else {
                    Log.d("DangerAreaRepository", "Cannot create danger area: It's not raining.")
                }
            } else {
                Log.d("DangerAreaRepository", "No weather data available for $location.")
            }
        }, location, latitude, longitude)
    }

    private fun saveDangerArea(dangerArea: DangerArea) {
        val newDangerAreaRef = database.push()
        newDangerAreaRef.setValue(dangerArea)
            .addOnSuccessListener {
                Log.d("DangerAreaRepository", "Danger area successfully created")
            }
            .addOnFailureListener {
                Log.e("DangerAreaRepository", "Failed to create danger area")
            }
    }
}
