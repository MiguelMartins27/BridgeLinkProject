package com.example.bridgelink

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bridgelink.users.User

@Composable
fun Profile(navController: NavController, signOut: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.navy_blue)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        ProfilePic(
            img = painterResource(id = R.drawable.eliseu),
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(20.dp))

        // Sign out button
        Button(
            onClick = {
                signOut() // Call the signOut function passed from MainActivity
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue))
        ) {
            Text(text = "Sign Out", color = Color.White)
        }

    }
}


@Composable
fun ProfilePic(img: Painter, modifier: Modifier = Modifier) {
    Image(
        painter = img,
        contentDescription = null,
        modifier = Modifier
            .size(250.dp)
            .clip(shape = CircleShape)
            .background(Color(0xFF8A2BE2))
    )
}

@Composable
fun ProfileInfo(userData: User, modifier: Modifier = Modifier) {
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
            Text( // Name
                text = userData.name,
                color = Color.White,
                fontSize = 36.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Blood Type: ${userData.bloodType}",
                    color = Color.White,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Height: ${userData.height}",
                    color = Color.White,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Weight: ${userData.weight}",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Column(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(10.dp))
                    .background(Color(0x994682B4))
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "${userData.deliveries}",
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
        }
    }
}
