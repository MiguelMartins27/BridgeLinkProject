package com.example.bridgelink.deliviries

data class Delivery(
    val key: String = "", // Add this line
    val user: String,
    val weight: Int,
    val size: String,
    val fragile: Boolean,
    val condition: String,
    val imageUri: String,
    val delivered: Boolean
)
