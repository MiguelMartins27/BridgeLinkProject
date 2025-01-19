package com.example.bridgelink

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bridgelink.users.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileSetupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileSetupScreen()
        }
    }

    @Composable
    fun ProfileSetupScreen() {
        val name by remember { mutableStateOf("") }
        val bloodType by remember { mutableStateOf("") }
        val height by remember { mutableStateOf("") }
        val weight by remember { mutableStateOf("") }
        val deliveries by remember { mutableStateOf("") }
        val distanceWalked by remember { mutableStateOf("") }
        val timefallExposure by remember { mutableStateOf("") }
        val dob by remember { mutableStateOf("") }

        // Image for profile (using resource as Int)
        var photoUrl by remember { mutableStateOf(0) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Complete your profile",
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )


            Button(onClick = { saveUserProfile(name, bloodType, height, weight, deliveries, distanceWalked, timefallExposure, dob, photoUrl) }) {
                Text("Save Profile")
            }
        }
    }

    private fun saveUserProfile(
        name: String,
        bloodType: String,
        height: String,
        weight: String,
        deliveries: String,
        distanceWalked: String,
        timefallExposure: String,
        dob: String,
        photoUrl: Int
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (userId != null) {
            val userProfile = User(
                name,
                bloodType,
                height,
                weight,
                deliveries,
                distanceWalked,
                timefallExposure,
                dob,
                photoUrl
            )

            FirebaseDatabase.getInstance().getReference("users").child(userId).setValue(userProfile)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
