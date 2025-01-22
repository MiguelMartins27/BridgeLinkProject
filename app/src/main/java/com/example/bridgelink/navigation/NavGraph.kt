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
import com.example.bridgelink.MainPage
import com.example.bridgelink.NewDeliveryScreen
import com.example.bridgelink.OdradekScanner
import com.example.bridgelink.Profile
import com.example.bridgelink.R
import com.example.bridgelink.SecondMainPage
import com.example.bridgelink.TraceRoute
import com.example.bridgelink.utils.SharedViewModel
import com.example.bridgelink.utils.RouteViewModel


@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier, sharedViewModel: SharedViewModel, signOut: () -> Unit, routeViewModel: RouteViewModel) {
    NavHost(
        navController = navController,
        startDestination = Screens.MainPage.route,
        modifier = modifier
    ) {
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
            Profile(navController = navController, signOut = signOut)
        }
        composable (route = Screens.NewDeliveryScreen.route) {
            NewDeliveryScreen(navController = navController)
        }
        composable (route = Screens.MainPage.route) {
            MainPage(navController = navController, sharedViewModel = sharedViewModel, routeViewModel = routeViewModel)
        }
        composable (route = Screens.SecondMainPage.route) {
            SecondMainPage(navController = navController)
        }
        composable (route = Screens.TraceRoute.route){
            TraceRoute(navController = navController, routeViewModel = routeViewModel)
        }
    }
}

