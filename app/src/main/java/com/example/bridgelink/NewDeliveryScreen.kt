package com.example.bridgelink

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color

@Composable
fun NewDeliveryScreen(navController: NavController) {
    var weight by remember { mutableStateOf("") }
    var selectedSize by remember { mutableStateOf("") }
    var packageType by remember { mutableStateOf("") }
    var fragile by remember { mutableStateOf(false) }
    var condition by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Open image picker
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Size Dropdown
        DropdownMenuField(
            label = "Size",
            options = listOf("S", "M", "L"),
            selectedValue = selectedSize,
            onValueChange = { selectedSize = it }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = fragile,
                onCheckedChange = { fragile = it }
            )
            Text("Fragile", modifier = Modifier.padding(start = 8.dp))
        }

        // Condition Dropdown
        DropdownMenuField(
            label = "Condition",
            options = listOf("Poor", "Medium", "Good", "Perfect"),
            selectedValue = condition,
            onValueChange = { condition = it }
        )

        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Select Package Image")
        }

        Button(
            onClick = {
                // Navigate to next step, passing the entered details
                navController.navigate("NearestPostOfficesScreen")
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Next")
        }
    }
}

@Composable
fun DropdownMenuField(
    label: String,
    options: List<String>,
    selectedValue: String,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        TextField(
            value = selectedValue,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .background(Color.LightGray.copy(alpha = 0.5f))
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

