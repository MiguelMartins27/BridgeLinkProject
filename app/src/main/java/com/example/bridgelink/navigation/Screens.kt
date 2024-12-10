package com.example.bridgelink.navigation

sealed class Screens (val route: String) {
    object Menu : Screens("menu_screen")
    object CargoManagement : Screens("cargo_screen")
    object ChiralNetworkMap : Screens("chiral_screen")
    object OdradekScanner : Screens("odradek_screen")
    object Profile : Screens("profile_screen")
    object ShipmentOptimizationMap : Screens("shipment_screen")
    object StrandConnectionsMap : Screens("strand_screen")
    object TimeFallForecast : Screens("timefall_screen")
}