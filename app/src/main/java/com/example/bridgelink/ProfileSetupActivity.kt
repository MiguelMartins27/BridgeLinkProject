package com.example.bridgelink

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

import androidx.compose.material3.Text
import androidx.compose.material3.TextField

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
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
        // State variables for input fields
        var name by remember { mutableStateOf("") }
        var bloodType by remember { mutableStateOf("") }
        var height by remember { mutableStateOf("") }
        var weight by remember { mutableStateOf("") }
        var dob by remember { mutableStateOf("") }
        var photoUrl by remember { mutableStateOf("") }
        var selectedPhotoResourceId by remember { mutableStateOf<Int?>(null) }

        val context = LocalContext.current  // Get context for Toast

        // Color values from your resources
        val backgroundColor = colorResource(id = R.color.navy_blue) // Navy Blue Background
        val textColor = colorResource(id = R.color.white) // White text
        val inputFieldColor = colorResource(id = R.color.home_grey) // Light Grey background for inputs
        val buttonColor = colorResource(id = R.color.blue) // Blue button color

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor), // Navy blue background
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            Text(
                text = "Complete your profile",
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Name input field
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name", color = Color.Black) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(inputFieldColor)
            )

            // Blood Type input field
            Dropdown(
                list = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
            ) { selectedBloodType ->
                bloodType = selectedBloodType
            }

            // Height input field (only numbers allowed)
            TextField(
                value = height,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        height = it
                    } else {
                        Toast.makeText(context, "Please enter a valid height", Toast.LENGTH_SHORT).show()
                    }
                },
                label = { Text("Height (cm)", color = Color.Black) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(inputFieldColor)
            )

            // Weight input field (only numbers allowed)
            TextField(
                value = weight,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        weight = it
                    } else {
                        Toast.makeText(context, "Please enter a valid weight", Toast.LENGTH_SHORT).show()
                    }
                },
                label = { Text("Weight (kg)", color = Color.Black) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(inputFieldColor)
            )

            DateInputField { selectedDob ->
                dob = selectedDob
            }

            // Profile Photo URL field (Optional, Horizontal Scroll for Image selection)
            ProfilePhotoSelectionField(
                selectedPhoto = selectedPhotoResourceId
            ) { selectedResourceId ->
                selectedPhotoResourceId = selectedResourceId
                photoUrl = selectedResourceId.toString() // Generate a URL or identifier for the photo.
            }

            // Save button with contrast
            Button(
                onClick = {
                    saveUserProfile(name, bloodType, height, weight, dob, photoUrl)
                },
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Save Profile", color = Color.White)
            }

        }
    }

    @Composable
    fun DateInputField(onDateChange: (String) -> Unit) {
        var textFieldValue by remember { mutableStateOf(TextFieldValue()) }

        TextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                val digitsOnly = newValue.text.filter { it.isDigit() }
                if (digitsOnly.length <= 8) {
                    val formattedText = formatDateInput(digitsOnly)
                    val newCursorPosition = calculateCursorPosition(digitsOnly.length)
                    textFieldValue = TextFieldValue(
                        text = formattedText,
                        selection = TextRange(newCursorPosition)
                    )
                    onDateChange(formattedText)
                }
            },
            label = { Text("Date of Birth (DD/MM/YYYY)") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color(0xFFF2F2F2))
        )
    }


    private fun formatDateInput(input: String): String {
        return buildString {
            input.forEachIndexed { index, char ->
                if (index == 2 || index == 4) append("/")
                append(char)
            }
        }
    }

    private fun calculateCursorPosition(digitsCount: Int): Int {
        return when {
            digitsCount > 4 -> digitsCount + 2
            digitsCount > 2 -> digitsCount + 1
            else -> digitsCount
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Dropdown(
        list: List<String>,
        onSelectionChanged: (String) -> Unit
    ) {
        var selectedText by remember { mutableStateOf(list[0]) }
        var isExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = !isExpanded },
        ) {
            TextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                list.forEach { text ->
                    DropdownMenuItem(
                        text = { Text(text = text) },
                        onClick = {
                            selectedText = text
                            isExpanded = false
                            onSelectionChanged(text)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }


    @Composable
    fun ProfilePhotoSelectionField(
        selectedPhoto: Int?,
        onPhotoSelected: (Int) -> Unit
    ) {
        val scrollState = rememberScrollState()
        val photos = listOf(
            R.drawable.eliseu,
            R.drawable.porter,
            // Add more drawable resources here
        )

        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            photos.forEach { photo ->
                Image(
                    painter = painterResource(id = photo),
                    contentDescription = "Profile Photo Icon",
                    modifier = Modifier
                        .size(50.dp)
                        .border(
                            width = 2.dp,
                            color = if (selectedPhoto == photo) Color.Blue else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onPhotoSelected(photo) }
                )
            }
        }
    }

    private fun saveUserProfile(
        name: String,
        bloodType: String,
        height: String,
        weight: String,
        dob: String,
        photoUrl: String
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (userId != null) {
            val userProfile = User(
                name,
                bloodType,
                height,
                weight,
                0, // Replace with actual values for user's blood pressure, cholesterol, etc.
                0,
                0,
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
