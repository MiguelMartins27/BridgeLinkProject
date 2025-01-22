package com.example.bridgelink

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun OdradekScanner(navController: NavController, modifier: Modifier = Modifier) {
    Box (
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.navy_blue))
    ) {


        Column (
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.padding(20.dp))

            Text(
                text = "Odradek Scanner",
                color = Color.White,
                fontSize = 40.sp,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.padding(50.dp))

            Image(
                painter = painterResource(id = R.drawable.scanner),
                contentDescription = "Odradek",
                modifier = Modifier
                    .padding(16.dp)
                    .size(400.dp)
            )
        }
    }
}

