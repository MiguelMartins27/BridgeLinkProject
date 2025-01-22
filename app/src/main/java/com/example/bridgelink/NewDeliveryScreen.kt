package com.example.bridgelink

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase
import java.io.File
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.bridgelink.deliveries.DeliveryRepository
import com.example.bridgelink.deliviries.Delivery
import com.example.bridgelink.navigation.Screens
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun NewDeliveryScreen(navController: NavHostController) {
    var weight by remember { mutableStateOf("") }
    var fragile by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedSize by remember { mutableStateOf("") }
    var selectedCondition by remember { mutableStateOf("") }
    val context = LocalContext.current
    var capturedImageUri by remember { mutableStateOf<Uri>(Uri.EMPTY) }
    var currentUser by remember { mutableStateOf("") }
    var downloadUrl by remember { mutableStateOf("") }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri?.let { uri ->
                uploadImageToFirebase(context, uri) { downloadUri, localUri ->
                    capturedImageUri = localUri
                    downloadUrl = downloadUri
                }
            }
        }
    }

    val selectImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uploadImageToFirebase(context, it) { downloadUri, localUri ->
                capturedImageUri = localUri
                downloadUrl = downloadUri
            }
        }
    }


    LaunchedEffect(Unit) {
        // Replace this with your actual method of getting the current user
        currentUser = getCurrentUser()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.navy_blue))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter Delivery Details",
            style = TextStyle(color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(vertical = 24.dp)
        )

        TextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") },
            modifier = Modifier.fillMaxWidth()
        )

        Dropdown(
            list = listOf("S", "M", "L"),
            label = "Package Size",
            onSelectionChanged = { selectedSize = it }
        )

        Dropdown(
            list = listOf("Poor", "Medium", "Good", "Perfect"),
            label = "Package Condition",
            onSelectionChanged = { selectedCondition = it }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = fragile, onCheckedChange = { fragile = it })
            Text("Fragile", color = Color.White, modifier = Modifier.padding(start = 8.dp))
        }


        if (capturedImageUri.path?.isNotEmpty() == true) {
            AsyncImage(
                model = capturedImageUri,
                contentDescription = "Captured photo",
                modifier = Modifier
                    .padding(50.dp)
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val photoFile = File.createTempFile("delivery_photo_", ".jpg", context.cacheDir).apply {
                        createNewFile()
                        deleteOnExit()
                    }
                    val photoUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        photoFile
                    )
                    imageUri = photoUri
                    cameraLauncher.launch(photoUri)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue))
            ) {
                Text("Take Photo")
            }


            Button(
                onClick = { selectImageLauncher.launch("image/*") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue))
            ) {
                Text("Choose Photo")
            }
        }

        Button(
            onClick = {
                val delivery = Delivery(
                    user = currentUser,
                    weight = weight.toIntOrNull() ?: 0,
                    size = selectedSize,
                    fragile = fragile,
                    condition = selectedCondition,
                    imageUri = downloadUrl,
                    delivered = false
                )
                CoroutineScope(Dispatchers.Main).launch {
                    val repository = DeliveryRepository()
                    val success = repository.saveDelivery(delivery)
                    if (success) {
                        showToast(context, "Delivery saved successfully")
                        navController.navigate(Screens.TraceRoute.route)
                    } else {
                        showToast(context, "Failed to save delivery")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue))
        ) {
            Text("Add package")
        }



        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = { navController.navigate(Screens.MainPage.route) },
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Main Page",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dropdown(
    list: List<String>,
    label: String = "Select Option",
    onSelectionChanged: (String) -> Unit
) {
    var selectedText by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    Column {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = !isExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = selectedText,
                onValueChange = {},
                label = { Text(text = label) },
                readOnly = true,
                singleLine = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                list.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            selectedText = item
                            isExpanded = false
                            onSelectionChanged(item)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

private fun uploadImageToFirebase(context: Context, uri: Uri) {
    val storage = Firebase.storage
    val storageRef = storage.reference.child("images/${uri.lastPathSegment}")

    val uploadTask = storageRef.putFile(uri)
    uploadTask.addOnSuccessListener {
        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
            saveImageUriToDatabase(downloadUri.toString())
        }
    }.addOnFailureListener { exception ->
        Log.e("FirebaseUpload", "Failed to upload image", exception)
        showToast(context, "Failed to upload image. Please try again.")
    }
}

private fun saveImageUriToDatabase(downloadUri: String) {
    val databaseRef = FirebaseDatabase.getInstance().getReference("images")
    val imageId = databaseRef.push().key ?: return
    databaseRef.child(imageId).setValue(downloadUri)
}

private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

private fun getCurrentUser(): String {
    val firebaseAuth = FirebaseAuth.getInstance()
    return firebaseAuth.currentUser?.uid ?: ""
}

private fun uploadImageToFirebase(context: Context, uri: Uri, onComplete: (String, Uri) -> Unit) {
    val storage = Firebase.storage
    val storageRef = storage.reference.child("images/${System.currentTimeMillis()}_${uri.lastPathSegment}")

    // Create a local copy of the image
    val localFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        localFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    storageRef.putFile(Uri.fromFile(localFile)).addOnSuccessListener {
        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
            onComplete(downloadUri.toString(), Uri.fromFile(localFile))
        }
    }.addOnFailureListener { exception ->
        Log.e("FirebaseUpload", "Failed to upload image", exception)
        showToast(context, "Failed to upload image. Please try again.")
        onComplete("", Uri.EMPTY)
    }
}






