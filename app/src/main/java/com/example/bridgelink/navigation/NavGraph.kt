package com.example.bridgelink.navigation


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bridgelink.DeliveriesScreen
import com.example.bridgelink.MainPage
import com.example.bridgelink.NewDeliveryScreen
import com.example.bridgelink.Profile
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
        composable (route = Screens.Deliverys.route) {
            DeliveriesScreen(navController = navController)
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

