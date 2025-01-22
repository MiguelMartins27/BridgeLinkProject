package com.example.bridgelink.users

import com.example.bridgelink.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepository {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")

    // Fetch user data
    fun fetchUserData(userId: String, onDataFetched: (User?) -> Unit) {
        database.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: ""
                    val bloodType = snapshot.child("bloodType").getValue(String::class.java) ?: ""
                    val height = snapshot.child("height").getValue(String::class.java) ?: ""
                    val weight = snapshot.child("weight").getValue(String::class.java) ?: ""
                    val deliveries = snapshot.child("deliveries").getValue(Int::class.java) ?: 0
                    val distanceWalked = snapshot.child("distanceWalked").getValue(Int::class.java) ?: 0
                    val timefallExposure = snapshot.child("timefallExposure").getValue(Int::class.java) ?: 0
                    val dob = snapshot.child("dob").getValue(String::class.java) ?: ""
                    val photoUrl = snapshot.child("photoUrl").getValue(String::class.java) ?: ""

                    // Create a User object
                    val user = User(name, bloodType, height, weight, deliveries, distanceWalked, timefallExposure, dob, photoUrl)
                    onDataFetched(user)
                } else {
                    onDataFetched(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    // Save new user data
    fun saveUserData(userId: String, user: User, onComplete: (Boolean) -> Unit) {
        val userRef = database.child(userId)
        userRef.setValue(user)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // Update existing user data
    fun updateUserData(userId: String, user: User, onComplete: (Boolean) -> Unit) {
        val userRef = database.child(userId)
        userRef.updateChildren(mapOf(
            "name" to user.name,
            "bloodType" to user.bloodType,
            "height" to user.height,
            "weight" to user.weight,
            "dob" to user.dob,
            "deliveries" to user.deliveries,
            "distanceWalked" to user.distanceWalked,
            "timefallExposure" to user.timefallExposure
        )).addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // Example list of user icons (just like your signal icons)
    val userIcons = listOf(
        R.drawable.eliseu,
        R.drawable.porter
        // Add your user icons here
    )
}