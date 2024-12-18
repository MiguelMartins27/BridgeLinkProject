package com.example.bridgelink.navigation


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

//importing the screens
import com.example.bridgelink.CargoManagement
import com.example.bridgelink.ChiralNetworkMap
import com.example.bridgelink.Menu
import com.example.bridgelink.Profile
import com.example.bridgelink.ShipmentOptimizationMap
import com.example.bridgelink.StrandConnectionsMap
import com.example.bridgelink.TimeFallForecast
import com.example.bridgelink.OdradekScanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.bridgelink.R



@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screens.StrandConnectionsMap.route,
        modifier = modifier
    ) {
        composable(route = Screens.Menu.route) {
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
        composable (route = Screens.ShipmentOptimizationMap.route) {
            ShipmentOptimizationMap(navController = navController)
        }
        composable (route = Screens.StrandConnectionsMap.route) {
            StrandConnectionsMap(navController = navController)
        }
        composable (route = Screens.TimeFallForecast.route) {
            TimeFallForecast(navController = navController)
        }
    }
}

@Composable
fun NavigationDots(currentPage: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.navy_blue))
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(8) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        if (currentPage == index) Color(0xFFFF5722)
                        else Color.Gray
                    )
            )
        }
    }
}

