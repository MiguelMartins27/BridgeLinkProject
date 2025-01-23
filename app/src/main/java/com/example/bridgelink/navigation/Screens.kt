package com.example.bridgelink.navigation

sealed class Screens(val route: String) {
    object Profile : Screens("profile")
    object MainPage : Screens("main_page")
    object SecondMainPage : Screens("second_main_page")
    object NewDeliveryScreen : Screens("new_delivery_screen")
    object TraceRoute : Screens("trace_route")
    object Deliverys : Screens("deliverys")
}