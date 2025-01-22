package com.example.bridgelink.utils

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.State

class RouteViewModel : ViewModel() {
    // List of routes, each route is a list of LatLng points
    private val _routes = mutableStateOf<List<List<LatLng>>>(emptyList())
    val routes: State<List<List<LatLng>>> = _routes

    // Add a new route
    fun addRoute(route: List<LatLng>) {
        _routes.value += listOf(route)
    }

    // Delete a route
    fun deleteRoute(routeIndex: Int) {
        _routes.value = _routes.value.filterIndexed { index, _ -> index != routeIndex }
    }
}