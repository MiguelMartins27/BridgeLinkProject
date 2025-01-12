package com.example.bridgelink.utils

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedViewModel : ViewModel() {
    private val _location = MutableStateFlow(Pair(0.0, 0.0))
    val location: StateFlow<Pair<Double, Double>> = _location.asStateFlow()

    fun updateLocation(latitude: Double, longitude: Double) {
        _location.value = Pair(latitude, longitude)
    }
}