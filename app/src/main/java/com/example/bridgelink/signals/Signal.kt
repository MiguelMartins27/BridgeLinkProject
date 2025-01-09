package com.example.bridgelink.signals

import java.util.UUID

data class Signal(
    val id: Int,
    val iconResourceId: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
    )
