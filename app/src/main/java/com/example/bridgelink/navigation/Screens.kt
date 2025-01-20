package com.example.bridgelink.navigation

sealed class Screens(val route: String) {
    object CargoManagement : Screens("cargo_management")
    object ChiralNetworkMap : Screens("chiral_network_map")
    object OdradekScanner : Screens("odradek_scanner")
    object Profile : Screens("profile")
    object MainPage : Screens("main_page")
    object SecondMainPage : Screens("second_main_page")
    object NewDeliveryScreen : Screens("new_delivery_screen")
}