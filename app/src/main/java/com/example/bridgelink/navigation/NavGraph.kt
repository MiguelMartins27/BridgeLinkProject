package com.example.bridgelink.navigation


//importing the screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bridgelink.CargoManagement
import com.example.bridgelink.ChiralNetworkMap
import com.example.bridgelink.Menu
import com.example.bridgelink.OdradekScanner
import com.example.bridgelink.Profile
import com.example.bridgelink.R
import com.example.bridgelink.StrandConnectionsMap
import com.example.bridgelink.TimeFallForecast


@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screens.MainPage.route,
        modifier = modifier
    ) {
        composable(route = Screens.LoginInicial.route) {
            Menu(navController = navController)
        }
        composable (route = Screens.CargoManagement.route) {
            CargoManagement(navController = navController)
        }
        composable (route = Screens.ChiralNetworkMap.route) {
            ChiralNetworkMap(navController = navController)
        }
        composable (route = Screens.OdradekScanner.route) {
            OdradekScanner(navController = navController)
        }
        composable (route = Screens.Profile.route) {
            Profile(navController = navController)
        }
        composable (route = Screens.MainPage.route) {
            StrandConnectionsMap(navController = navController)
        }
        composable (route = Screens.TimeFallForecast.route) {
            TimeFallForecast(navController = navController)
        }
    }
}

@Composable
fun NavigationDots(
    currentPage: Int,
    modifier: Modifier = Modifier // Add a modifier parameter with a default value
) {
    Column(
        modifier = modifier // Apply the passed modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.navy_blue))
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            repeat(8) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(
                            if (currentPage == index) 50.dp
                            else 40.dp
                        )
                        .clip(CircleShape)
                        .background(
                            if (currentPage == index) Color(0xFFFF5722)
                            else Color.Gray
                        )
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}


