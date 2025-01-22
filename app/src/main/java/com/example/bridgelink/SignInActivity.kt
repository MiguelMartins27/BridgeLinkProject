package com.example.bridgelink

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            goToMainActivity()
        } else {
            setContent {
                SignInScreen()
            }
        }
    }

    @Composable
    fun SignInScreen() {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var loading by remember { mutableStateOf(false) }
        var isSignInMode by remember { mutableStateOf(true) }
        var isPasswordVisible by remember { mutableStateOf(false) }
        var emailError by remember { mutableStateOf("") } // To hold error message for email
        var passwordError by remember { mutableStateOf("") } // To hold error message for password

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.navy_blue)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo and title
            Image(
                painter = painterResource(id = R.drawable.no_bg),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 32.dp)
            )

            Text(
                text = "Welcome to BridgeLink",
                style = TextStyle(color = Color.White, fontSize = 24.sp),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email input
            Text(
                text = "Email",
                style = TextStyle(color = Color.White, fontSize = 16.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            BasicTextField(
                value = email,
                onValueChange = { email = it },
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small)
                    .padding(16.dp)
            )
            if (emailError.isNotEmpty()) {
                Text(
                    text = emailError,
                    color = Color.Red,
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Password input
            Text(
                text = "Password",
                style = TextStyle(color = Color.White, fontSize = 16.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            BasicTextField(
                value = password,
                onValueChange = { password = it },
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small)
                    .padding(16.dp)
            )

            TextButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Text(
                    text = if (isPasswordVisible) "Hide Password" else "Show Password",
                    style = TextStyle(color = colorResource(id = R.color.blue))
                )
            }

            if (passwordError.isNotEmpty()) {
                Text(
                    text = passwordError,
                    color = Color.Red,
                    style = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign-in/Register button
            Button(
                onClick = {
                    loading = true
                    // Validate input fields
                    if (email.isBlank()) {
                        emailError = "Email is required"
                        loading = false
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailError = "Invalid email format"
                        loading = false
                    } else if (password.isBlank()) {
                        passwordError = "Password is required"
                        loading = false
                    } else if (password.length < 6) {
                        passwordError = "Password should be at least 6 characters"
                        loading = false
                    } else {
                        emailError = ""
                        passwordError = ""
                        if (isSignInMode) {
                            launchSignInFlow(email, password) {
                                emailError = it.first
                                passwordError = it.second
                            }
                        } else {
                            launchRegistrationFlow(email, password) {
                                emailError = it.first
                                passwordError = it.second
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue))
            ) {
                Text(text = if (isSignInMode) "Sign In" else "Register", style = TextStyle(color = Color.White))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Switch between sign-in and register mode
            TextButton(
                onClick = { isSignInMode = !isSignInMode }
            ) {
                Text(
                    text = if (isSignInMode) "Don't have an account? Register here" else "Already have an account? Sign In",
                    style = TextStyle(color = colorResource(id = R.color.blue))
                )
            }
        }
    }



    private fun launchSignInFlow(email: String, password: String, onError: (Pair<String, String>) -> Unit) {
        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Sign-in successful!")
                    goToMainActivity()
                } else {
                    Log.w(TAG, "Sign-in failed", task.exception)
                    val errorMessage = task.exception?.localizedMessage ?: "Unknown error"
                    // Set error message for the fields
                    onError(Pair("Invalid email or password.", ""))
                }
            }
    }

    private fun launchRegistrationFlow(email: String, password: String, onError: (Pair<String, String>) -> Unit) {
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Registration successful!")
                    goToProfileSetupActivity()
                } else {
                    Log.w(TAG, "Registration failed", task.exception)
                    val errorMessage = task.exception?.localizedMessage ?: "Unknown error"
                    // Set error messages for the fields
                    onError(Pair("Invalid email format.", "Password should be at least 6 characters."))
                }
            }
    }


    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun goToProfileSetupActivity() {
        startActivity(Intent(this, ProfileSetupActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "SignInActivity"
    }
}
