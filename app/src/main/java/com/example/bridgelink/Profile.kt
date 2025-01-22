package com.example.bridgelink

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* // ktlint-disable no-wildcard-imports
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bridgelink.navigation.Screens
import com.example.bridgelink.users.User
import com.example.bridgelink.users.UserRepository
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Profile(navController: NavController, signOut: () -> Unit, modifier: Modifier = Modifier) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid
    val userRepository = remember { UserRepository() }

    var userData by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId != null) {
            userRepository.fetchUserData(userId) { user ->
                userData = user
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.navy_blue)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp)) // Reduced space for better visibility
        ProfilePic(
            img = painterResource(id = userData?.photoUrl?.let { userRepository.userIcons.find { icon -> icon == it.toInt() } } ?: R.drawable.eliseu),
            modifier = Modifier.padding(16.dp)
        )
        if (isLoading) {
            Text(text = "Loading...", color = Color.White, fontSize = 20.sp)
        } else {
            userData?.let { data ->
                ProfileInfo(
                    navController,
                    userData = data,
                    signOut = signOut,
                )
            } ?: Text(text = "No user data found", color = Color.White, fontSize = 20.sp)
        }
    }
}

@Composable
fun ProfilePic(img: Painter, modifier: Modifier = Modifier) {
    Image(
        painter = img,
        contentDescription = null,
        modifier = Modifier
            .size(150.dp) // Reduced size for better layout
            .clip(shape = CircleShape)
            .background(Color(0xFF8A2BE2))
    )
}

@Composable
fun EditableField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            unfocusedTextColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedContainerColor = Color(0x33FFFFFF),
            focusedContainerColor = Color(0x33FFFFFF),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
fun ProfileInfo(
    navController: NavController,
    userData: User,
    signOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.navy_blue))
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = userData.name,
                color = Color.White,
                fontSize = 36.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Blood Type: ${userData.bloodType}",
                    color = Color.White,
                    fontSize = 20.sp
                )
                Text(
                    text = "Height: ${userData.height}",
                    color = Color.White,
                    fontSize = 20.sp
                )
                Text(
                    text = "Weight: ${userData.weight}",
                    color = Color.White,
                    fontSize = 20.sp
                )
                Text(
                    text = "Date of Birth: ${userData.dob}",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(10.dp))
                    .background(Color(0x994682B4))
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Deliveries: ${userData.deliveries}",
                    color = Color.White,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Total distance walked: ${userData.distanceWalked}",
                    color = Color.White,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Total Timefall exposure: ${userData.timefallExposure}",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = signOut,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue))
                    ) {
                        Text("Sign Out")
                    }

                    IconButton(
                        onClick = { navController.navigate(Screens.SecondMainPage.route) },
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(colorResource(id = R.color.blue)),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Go Back",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}
