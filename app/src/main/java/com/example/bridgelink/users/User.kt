package com.example.bridgelink.users

data class User(
    val name: String,
    val bloodType: String,
    val height: String,
    val weight: String,
    val deliveries: Int,
    val distanceWalked: Int,
    val timefallExposure: Int,
    val dob: String,
    val photoUrl: String
)