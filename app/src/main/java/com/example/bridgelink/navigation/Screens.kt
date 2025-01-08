package com.example.bridgelink.navigation

sealed class Screens (val route: String) {
    object LoginInicial : Screens("login_inicial")
    object CargoManagement : Screens("cargo_screen")
    object ChiralNetworkMap : Screens("chiral_screen")
    object OdradekScanner : Screens("odradek_screen")
    object Profile : Screens("profile_screen")
    object MainPage : Screens("main_page")
    object TimeFallForecast : Screens("timefall_screen")
    object SecondMainPage : Screens("second_main_page")
}