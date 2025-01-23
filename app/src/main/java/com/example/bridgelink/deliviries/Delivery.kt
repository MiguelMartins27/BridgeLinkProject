package com.example.bridgelink.deliviries

data class Delivery(
    val key: String = "",           // Default value for key
    val user: String = "",          // Default value for user
    val weight: Int = 0,            // Default value for weight
    val size: String = "",          // Default value for size
    val fragile: Boolean = false,   // Default value for fragile
    val condition: String = "",     // Default value for condition
    val imageUri: String = "",      // Default value for imageUri
    val delivered: Boolean = false  // Default value for delivered
) {
    // No-argument constructor is implicitly provided by the default values in data class.
}

