package com.example.bridgelink.signals

import java.util.UUID

data class Signal(
    val iconResourceName: String, // Changed from iconUrl to iconResourceName
    val description: String,
    val latitude: Double,
    val longitude: Double
)
