package com.example.bridgelink.utils

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Sign out the user
    fun signOut() {
        auth.signOut()
    }

    // Check if user is signed in
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    // Get user display name
    fun getUserName(): String? {
        return auth.currentUser?.displayName ?: ANONYMOUS
    }

    // Get user photo URL
    fun getUserPhotoUrl(): String? {
        return auth.currentUser?.photoUrl?.toString()
    }

    companion object {
        const val ANONYMOUS = "anonymous"
    }
}
