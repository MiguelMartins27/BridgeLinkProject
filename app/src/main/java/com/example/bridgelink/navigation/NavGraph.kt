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
import com.example.bridgelink.OdradekScanner
import com.example.bridgelink.Profile
import com.example.bridgelink.ShipmentOptimizationMap
import com.example.bridgelink.StrandConnectionsMap
import com.example.bridgelink.TimeFallForecast

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screens.Menu.route,
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