package com.example.bridgelink

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.bridgelink.navigation.Screens

@Composable
fun SecondMainPage(navController: NavHostController, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.navy_blue))
            .padding(16.dp)
    ) {
        // Column for other buttons and text
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(bottom = 80.dp), // Provide space for bottom button
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Choose an option",
                modifier = Modifier.padding(bottom = 32.dp),
                style = TextStyle(color = colorResource(id = R.color.white), fontSize = 20.sp)
            )

            // Profile Button
            Button(
                onClick = {
                    // Navigate to profile screen
                    navController.navigate(Screens.Profile.route)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue))
            ) {
                Text(text = "Profile", color = androidx.compose.ui.graphics.Color.White)
            }

            // Odradek Scanner Button
            Button(
                onClick = {
                    // Navigate to odradek scanner screen
                    navController.navigate(Screens.OdradekScanner.route)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue))
            ) {
                Text(text = "Odradek Scanner", color = androidx.compose.ui.graphics.Color.White)
            }

            // Cargo Management Button
            Button(
                onClick = {
                    // Navigate to cargo management screen
                    navController.navigate(Screens.CargoManagement.route)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue))
            ) {
                Text(text = "Cargo Management", color = androidx.compose.ui.graphics.Color.White)
            }

            // Chiral Network Map Button
            Button(
                onClick = {
                    // Navigate to chiral network map screen
                    navController.navigate(Screens.ChiralNetworkMap.route)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue))
            ) {
                Text(text = "Chiral Network Map", color = androidx.compose.ui.graphics.Color.White)
            }
        }

        // Floating Image Button at Bottom Center
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp) // Adjust the padding as needed
                .size(72.dp)
                .clickable { navController.navigate(Screens.MainPage.route) }
        ) {
            IconButton(
                onClick = { navController.navigate(Screens.MainPage.route) },
                modifier = Modifier.align(Alignment.CenterVertically),
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
            ) {
                // Using the close icon (cross)
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Main Page",
                    modifier = Modifier.size(72.dp) // Adjust the size as needed
                )
            }
        }
    }
}

