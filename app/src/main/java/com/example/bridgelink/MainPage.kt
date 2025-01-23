package com.example.bridgelink


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AddReaction
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.bridgelink.danger.area.DangerAreaRepository
import com.example.bridgelink.navigation.Screens
import com.example.bridgelink.post.office.PostOfficeRepository
import com.example.bridgelink.signals.Signal
import com.example.bridgelink.signals.SignalRepository
import com.example.bridgelink.utils.RouteViewModel
import com.example.bridgelink.utils.SharedViewModel
import com.example.bridgelink.weatherinfo.WeatherInfo
import com.example.bridgelink.weatherinfo.WeatherInfoRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.JsonObject
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotation
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow

@Composable
fun MainPage(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    routeViewModel: RouteViewModel
) {
    val locationState = sharedViewModel.location.collectAsState()
    val (latitude, longitude) = locationState.value
    val mapViewportState = rememberMapViewportState()
    val routes = routeViewModel.routes.value
    var mapView by remember { mutableStateOf<MapView?>(null) }

    // Coroutine job to periodically refresh weather data
    val fetchJob = rememberUpdatedState {
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                mapView?.let { map ->
                    addTimefallLayer(map)
                }
                delay(60000)
            }
        }
    }

    // Initial weather fetch when the page loads
    LaunchedEffect(mapView) {
        fetchJob.value.invoke()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.navy_blue))
    ) {
        MapboxMap(
            Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
            style = { MapStyle(style = "mapbox://styles/miguelmartins27/cm4k61vj1007501si3wux1brp") }
        ) {
            MapEffect(Unit) { map ->
                mapView = map
                map.location.updateSettings {
                    locationPuck = createDefault2DPuck(withBearing = true)
                    enabled = true
                    puckBearing = PuckBearing.COURSE
                    puckBearingEnabled = true
                }
                mapViewportState.transitionToFollowPuckState()

                map.getMapboxMap().getStyle { style ->
                    addPostOfficesLayer(map)
                    addSignalsLayer(map)
                    addWeatherBasedDangerLayer(map)

                    routes.forEachIndexed { index, route ->
                        val lineString = LineString.fromLngLats(route.map { Point.fromLngLat(it.longitude, it.latitude) })
                        if (!style.styleSourceExists("route-source-$index")) {
                            style.addSource(geoJsonSource("route-source-$index") {
                                geometry(lineString)
                            })
                            style.addLayerBelow(
                                lineLayer("route-layer-$index", "route-source-$index") {
                                    lineColor("#0000FF")
                                    lineWidth(5.0)
                                },
                                "location-indicator-layer"
                            )
                        }
                    }
                }
            }
        }

        // Routes List
        LazyColumn(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .width(150.dp)
                .background(Color(0x88000000), RoundedCornerShape(8.dp))
        ) {
            items(routes.size) { index ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Route ${index + 1}",
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            mapView?.let { map ->
                                map.getMapboxMap().getStyle { style ->
                                    style.removeStyleLayer("route-layer-$index")
                                    style.removeStyleSource("route-source-$index")
                                }
                                routeViewModel.deleteRoute(index)
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete Route",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Bottom Navigation Buttons
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
                .clickable { navController.navigate(Screens.SecondMainPage.route) }
                .size(72.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.no_bg),
                contentDescription = "Floating Image Button"
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 26.dp)
                .size(56.dp)
                .clickable { navController.navigate(Screens.NewDeliveryScreen.route) }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(52.dp)
            )
        }

        mapView?.let {
            InteractionUtils(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = 56.dp),
                latitude = latitude,
                longitude = longitude,
                mapView = it
            )
        }

        InteractionUtilsLayerControl(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = (-28).dp)
        )
    }
}



@Composable
fun InteractionUtilsLayerControl(
    modifier: Modifier = Modifier,
) {
    var isChiralNetworkLayerEnabled by remember { mutableStateOf(true) }
    var isSignalsLayerEnabled by remember { mutableStateOf(true) }

    Row(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 0.dp,
                    bottomEnd = 0.dp,
                    bottomStart = 16.dp
                )
            )
            .background(Color(0xE64682B4))
            .height(64.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Chiral Network Layer
        IconToggleButton(
            iconResId = R.drawable.signals,
            contentDescription = "Toggle Chiral Network Layer",
            onClick = {
                isChiralNetworkLayerEnabled = !isChiralNetworkLayerEnabled
                toggleAnnotation("ChiralNetwork", isChiralNetworkLayerEnabled)
            }
        )
        Spacer(modifier = Modifier.width(8.dp))

        // Signals Layer
        IconToggleButton(
            iconResId = R.drawable.ds_signs_trouble,
            contentDescription = "Toggle Signals Layer",
            onClick = {
                isSignalsLayerEnabled = !isSignalsLayerEnabled
                toggleAnnotation("Signals", isSignalsLayerEnabled)
            }
        )
    }
}

@Composable
fun InteractionUtils(modifier: Modifier = Modifier, latitude: Double, longitude: Double, mapView: MapView) {
    var showAddSignalDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .width(56.dp) // Reduced width
            .clip(
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 0.dp,
                    bottomEnd = 0.dp,
                    bottomStart = 16.dp
                )
            ) // Slightly smaller corner radius
            .background(Color(0xE64682B4))
            .height(200.dp) // Reduced height
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp), // Reduced padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { showAddSignalDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.AddReaction,
                    contentDescription = "Add Signal",
                    tint = Color.White,
                    modifier = Modifier
                        .size(36.dp) // Reduced icon size
                        .padding(end = 4.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp), // Reduced padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            IconButton(
                    onClick = {
                        mapView?.let { map ->
                            map.camera.easeTo(
                                CameraOptions.Builder()
                                    .center(Point.fromLngLat(longitude, latitude)) // Set camera to user's location
                                    .zoom(15.0) // Adjust zoom level as needed
                                    .build(),
                                MapAnimationOptions.mapAnimationOptions {
                                    duration(1000) // Smooth animation duration in milliseconds
                                }
                            )
                        }
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation, // Use a location icon
                    contentDescription = "Center Map",
                    tint = Color.White,
                    modifier = Modifier
                        .size(36.dp) // Reduced icon size
                        .padding(end = 4.dp)
            )
        }
        }

        //Row(
        //    modifier = Modifier
        //        .padding(horizontal = 4.dp), // Reduced padding
        //    verticalAlignment = Alignment.CenterVertically,
        //    horizontalArrangement = Arrangement.Center
        //) {
        //    IconButton(
        //        onClick = {  }
        //    ) {
        //        Icon(
        //            imageVector = Icons.Default.AddAlert,
        //            contentDescription = null,
        //            tint = Color.White,
        //            modifier = Modifier
        //                .size(36.dp) // Reduced icon size
        //                .padding(end = 4.dp)
        //        )
        //    }
        //}
//
        //Row(
        //    modifier = Modifier
        //        .weight(1f)
        //        .padding(horizontal = 4.dp), // Reduced padding
        //    verticalAlignment = Alignment.CenterVertically,
        //    horizontalArrangement = Arrangement.Center
        //) {
        //    IconButton(
        //        onClick = { }
        //    ) {
        //        Icon(
        //            imageVector = Icons.Default.Bloodtype,
        //            contentDescription = null,
        //            tint = Color.Red,
        //            modifier = Modifier
        //                .size(36.dp) // Reduced icon size
        //                .padding(end = 4.dp)
        //        )
        //    }
        //}
    }
    if (showAddSignalDialog) {
        AddSignalDialog(
            onDismiss = { showAddSignalDialog = false },
            latitude = latitude,
            longitude = longitude
        )
    }
}

@Composable
fun AddSignalDialog(
    onDismiss: () -> Unit,
    latitude: Double,
    longitude: Double,
) {
    var selectedIcon by remember { mutableStateOf<Int?>(null) }
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF34343C),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Add Signal", fontWeight = FontWeight.Bold, color = Color.White)

                LazyRow {
                    items(SignalRepository().signalIcons) { iconId ->
                        Image(
                            painter = painterResource(id = iconId),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { selectedIcon = iconId }
                                .background(
                                    if (selectedIcon == iconId) Color.LightGray else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(4.dp)
                        )
                    }
                }

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(colorResource(id = R.color.blue))) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            selectedIcon?.let { iconId ->
                                onAddSignal(iconId.toString(), description, latitude, longitude)
                                onDismiss()
                            }
                        },
                        enabled = selectedIcon != null && description.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.blue))
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}


private lateinit var postOfficeManager: PointAnnotationManager
private val postOfficeAnnotations = mutableListOf<PointAnnotation>()

private lateinit var ChiralNetworkManager: CircleAnnotationManager
private val ChiralNetworkAnnotations = mutableListOf<CircleAnnotation>()

private lateinit var signalManager: PointAnnotationManager
private val signalAnnotations = mutableListOf<PointAnnotation>()

private lateinit var timefallManager: PointAnnotationManager
private val timefallAnnotations = mutableListOf<PointAnnotation>()

fun addPostOfficesLayer(mapView: MapView) {
    val postOfficeRepository = PostOfficeRepository()
    postOfficeRepository.fetchPostOffices { postOffices ->
        mapView.mapboxMap.getStyle {
            val annotationApi = mapView.annotations
            postOfficeManager = annotationApi.createPointAnnotationManager()

            val originalBitmap = BitmapFactory.decodeResource(mapView.context.resources, R.drawable.postbox)
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, originalBitmap.width / 8, originalBitmap.height / 8, false)

            postOffices.forEach { postOffice ->
                val pointAnnotationOptions = PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(postOffice.longitude, postOffice.latitude))
                    .withIconImage(scaledBitmap)
                    .withData(JsonObject().apply { addProperty("name", postOffice.name) })

                val annotation = postOfficeManager.create(pointAnnotationOptions)
                postOfficeAnnotations.add(annotation)
            }

            // Listen for zoom level changes
            mapView.mapboxMap.addOnCameraChangeListener {
                val currentZoom = mapView.getMapboxMap().cameraState.zoom

                // Define the zoom range where the annotations should be visible
                val shouldBeVisible = currentZoom in 14.0..18.0

                // Update the opacity of annotations based on the zoom level
                postOfficeAnnotations.forEach { annotation ->
                    annotation.iconOpacity = if (shouldBeVisible) 1.0 else 0.0
                    postOfficeManager.update(annotation)
                }
            }
        }
    }
}

fun addWeatherBasedDangerLayer(mapView: MapView) {
    mapView.mapboxMap.getStyle { style ->
        val annotationApi = mapView.annotations
        ChiralNetworkManager = annotationApi.createCircleAnnotationManager()

        // Reference to Firebase
        val weatherRef = FirebaseDatabase.getInstance().reference.child("weatherCurrentInApp")

        weatherRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear existing annotations
                ChiralNetworkAnnotations.clear()
                ChiralNetworkManager.deleteAll()

                // Iterate through each location in Firebase
                snapshot.children.forEach { locationSnapshot ->
                    val condition = locationSnapshot.child("condition").getValue(String::class.java) ?: ""
                    val latitude = locationSnapshot.child("latitude").getValue(Double::class.java) ?: 0.0
                    val longitude = locationSnapshot.child("longitude").getValue(Double::class.java) ?: 0.0

                    // Check if it's raining
                    if (condition.contains("rain", ignoreCase = true) ||
                        condition.contains("showers", ignoreCase = true)
                    ) {
                        val radiusInMeters = 100.0
                        val dangerArea = CircleAnnotationOptions()
                            .withPoint(Point.fromLngLat(longitude, latitude))
                            .withCircleOpacity(0.5)
                            .withCircleColor("#FF0000")
                            .withCircleRadius(radiusInMeters)

                        // Add the circle annotation to the map
                        val annotation = ChiralNetworkManager.create(dangerArea)
                        ChiralNetworkAnnotations.add(annotation)

                        Log.d("WeatherBasedDangerLayer",
                            "Danger area added at $latitude, $longitude for condition: $condition")
                    } else {
                        Log.d("WeatherBasedDangerLayer",
                            "No rain detected at ${locationSnapshot.key} with condition: $condition")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("WeatherBasedDangerLayer", "Error fetching weather data: ${error.message}")
            }
        })

        // Add zoom change listener
        mapView.getMapboxMap().addOnCameraChangeListener {
            val currentZoom = mapView.getMapboxMap().cameraState.zoom
            val shouldBeVisible = currentZoom in 10.0..18.0

            ChiralNetworkAnnotations.forEach { annotation ->
                annotation.circleOpacity = if (shouldBeVisible) 0.5 else 0.0

                // Calculate new radius based on zoom level
                val baseRadius = 100.0 // Base radius in meters
                val newRadius = baseRadius * 2.0.pow(currentZoom - 14.0)
                annotation.circleRadius = newRadius

                ChiralNetworkManager.update(annotation)
            }
        }
    }
}


fun addSignalsLayer(mapView: MapView) {
    val signalRepository = SignalRepository()
    signalRepository.fetchSignals { signals ->
        mapView.mapboxMap.getStyle { style ->
            val annotationApi = mapView.annotations
            signalManager = annotationApi.createPointAnnotationManager()

            signals.forEach { signal ->
                val iconResourceId = signal.iconResourceName.toInt()
                if (iconResourceId != 0) {
                    val originalBitmap = BitmapFactory.decodeResource(mapView.context.resources, iconResourceId)
                    val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, originalBitmap.width / 8, originalBitmap.height / 8, false)
                    val pointAnnotationOptions = PointAnnotationOptions()
                        .withPoint(Point.fromLngLat(signal.longitude, signal.latitude))
                        .withIconImage(scaledBitmap)
                        .withData(JsonObject().apply {
                            addProperty("description", signal.description)
                        })

                    val annotation = signalManager.create(pointAnnotationOptions)
                    signalAnnotations.add(annotation)
                } else {
                    Log.e("MyTag", "Icon resource not found for: ${signal.iconResourceName}")
                }
            }

            // Listen for zoom level changes
            mapView.mapboxMap.addOnCameraChangeListener {
                val currentZoom = mapView.getMapboxMap().cameraState.zoom

                // Define the zoom range where the annotations should be visible
                val shouldBeVisible = currentZoom in 14.0..18.0

                // Update the opacity of annotations based on the zoom level
                signalAnnotations.forEach { annotation ->
                    annotation.iconOpacity = if (shouldBeVisible) 1.0 else 0.0
                    signalManager.update(annotation)
                }
            }
        }
    }
}

fun addTimefallLayer(mapView: MapView) {
    // List of location names
    val locations = listOf("California", "Lisbon", "Tokyo", "Cagliari")

    // List of coordinates for each location
    val locationCoordinates1 = listOf(
        LocationPoint(38.8100, -9.2285), // North of Casal de Cambra (California)
        LocationPoint(38.8009, -9.2150), // East of Casal de Cambra (Lisboa)
        LocationPoint(38.7900, -9.2285), // South of Casal de Cambra (Tokyo)
        LocationPoint(38.8009, -9.2420)  // West of Casal de Cambra (Cagliari)
    )

    val locationCoordinates = listOf(
        LocationPoint(38.7589, -9.1569), // North of FCUL
        LocationPoint(38.7550, -9.1515), // East of FCUL
        LocationPoint(38.7500, -9.1570), // South of FCUL
        LocationPoint(38.7550, -9.1625)  // West of FCUL
    )


    val weatherInfoRepository = WeatherInfoRepository()

    // Start a coroutine to run the weather data fetch concurrently
    CoroutineScope(Dispatchers.Main).launch {
        val deferredResults = locations.zip(locationCoordinates).map { (location, coordinates) ->
            // Use async to run each weather fetch concurrently
            async {
                // Fetch weather data for the current location using location name and coordinates
                weatherInfoRepository.fetchWeather({ weatherList ->
                    mapView.getMapboxMap().getStyle { style ->
                        val annotationApi = mapView.annotations
                        timefallManager = annotationApi.createPointAnnotationManager()

                        weatherList.forEach { weather ->
                            val iconResourceId = weather.iconResourceId
                            Log.d("WeatherLayer", "Adding weather icon for $location: $iconResourceId")
                            val originalBitmap = BitmapFactory.decodeResource(mapView.context.resources, iconResourceId)
                            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, originalBitmap.width, originalBitmap.height, false)
                            val point = Point.fromLngLat(coordinates.longitude, coordinates.latitude)
                            val pointAnnotationOptions = PointAnnotationOptions()
                                .withPoint(point) // Use coordinates
                                .withIconImage(scaledBitmap)
                                .withData(JsonObject().apply {
                                    addProperty("temperature", weather.temperature)
                                    addProperty("condition", weather.condition)
                                    addProperty("latitude", weather.latitude)
                                    addProperty("longitude", weather.longitude)
                                })

                            val annotation = timefallManager.create(pointAnnotationOptions)
                            timefallAnnotations.add(annotation)
                        }
                    }
                }, location, coordinates.latitude, coordinates.longitude) // Pass the location name and coordinates
            }
        }

        // Wait for all async operations to complete
        deferredResults.awaitAll()
    }
}

fun addTimefallLayer2(mapView: MapView) {
    val locations = listOf("California", "Lisbon", "Tokyo", "Cagliari")
    val locationKeys = mutableListOf<String>()
    val weatherData = mutableListOf<WeatherInfo>()

    // Map coordinates explicitly to locations
    val coordinatesMap1 = mapOf(
        "California" to LocationPoint(38.8100, -9.2285),
        "Lisbon" to LocationPoint(38.8009, -9.2150),
        "Tokyo" to LocationPoint(38.7900, -9.2285),
        "Cagliari" to LocationPoint(38.8009, -9.2420)
    )

    val coordinatesMap = mapOf(
        "North of FCUL" to LocationPoint(38.7589, -9.1569), // North
        "East of FCUL" to LocationPoint(38.7550, -9.1515),  // East
        "South of FCUL" to LocationPoint(38.7500, -9.1570), // South
        "West of FCUL" to LocationPoint(38.7550, -9.1625)   // West
    )


    val weatherInfoRepository = WeatherInfoRepository()

    // Step 1: Retrieve location keys concurrently using async
    CoroutineScope(Dispatchers.Main).launch {
        val deferredLocationKeys = locations.map { location ->
            async {
                val locationKey = weatherInfoRepository.fetchLocationKey(location)
                locationKey?.also {
                    locationKeys.add(it)
                } ?: Log.e("WeatherLayer", "Location key not found for: $location")
            }
        }

        // Await the result of all async tasks and collect the location keys
        deferredLocationKeys.awaitAll()

        // Step 2: Fetch weather data concurrently for each location key
        val deferredWeatherData = locationKeys.map { locationKey ->
            async {
                val coordinates = coordinatesMap[locations.find { it.contains(locationKey) }]
                coordinates?.let {
                    weatherInfoRepository.fetchWeatherData(locationKey, it.latitude, it.longitude)
                }
            }
        }

        // Await the result of all async tasks and collect the weather data
        deferredWeatherData.awaitAll().forEach { weather ->
            weather?.let { weatherData.add(it) }
        }

        // Step 3: Add weather data to map
        addWeatherDataToMap(weatherData, mapView)
    }
}

// Step 3: Add weather data to map
private fun addWeatherDataToMap(weatherData: List<WeatherInfo>, mapView: MapView) {
    mapView.getMapboxMap().getStyle {
        val annotationApi = mapView.annotations
        timefallManager = annotationApi.createPointAnnotationManager()  // Use PointAnnotationManager for icons

        // Loop through all weather data and add annotations
        weatherData.forEach { weatherInfo ->
            val point = Point.fromLngLat(weatherInfo.longitude, weatherInfo.latitude)
            val iconResourceId = weatherInfo.iconResourceId
            val originalBitmap = BitmapFactory.decodeResource(mapView.context.resources, iconResourceId)
            val displayMetrics = mapView.context.resources.displayMetrics
            val scaledBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                originalBitmap.width * displayMetrics.density.toInt(),
                originalBitmap.height * displayMetrics.density.toInt(),
                false
            )

            // Create the PointAnnotationOptions
            val pointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point) // Set the point for the annotation
                .withIconImage(scaledBitmap) // Set the icon image (scaled bitmap)
                .withData(JsonObject().apply {
                    addProperty("temperature", weatherInfo.temperature)
                    addProperty("condition", weatherInfo.condition)
                    addProperty("latitude", weatherInfo.latitude)
                    addProperty("longitude", weatherInfo.longitude)
                })

            // Create the point annotation and add it to the manager
            val pointAnnotation = timefallManager.create(pointAnnotationOptions)
            timefallAnnotations.add(pointAnnotation)
        }
    }
}


fun toggleAnnotation(type: String, isVisible: Boolean) {
    when (type) {
        "ChiralNetwork" -> {
            if (::ChiralNetworkManager.isInitialized) {
                // Ensure ChiralNetworkAnnotations are initialized and updated properly
                ChiralNetworkAnnotations.forEach { annotation ->
                    // Toggle visibility based on the zoom level or the visibility flag
                    annotation.circleOpacity = if (isVisible) 0.5 else 0.0
                    ChiralNetworkManager.update(annotation)
                }
            } else {
                Log.e("ChiralNetwork", "ChiralNetworkManager not initialized.")
            }
        }
        "Signals" -> {
            if (::signalManager.isInitialized) {
                // Toggle signal annotations' visibility
                signalAnnotations.forEach { annotation ->
                    annotation.iconOpacity = if (isVisible) 1.0 else 0.0
                    signalManager.update(annotation)
                }
            } else {
                Log.e("Signals", "SignalManager not initialized.")
            }
        }
        else -> {
            Log.e("toggleAnnotation", "Unknown type: $type")
        }
    }
}

fun onAddSignal(icon: String, description: String, latitude: Double, longitude: Double) {
    val signal = Signal(
        iconResourceName = icon,
        description = description,
        latitude = latitude,
        longitude = longitude,
    )
    val signalRepository = SignalRepository()

    signalRepository.saveSignal(signal) { success ->
        if (success) {
            // Handle success (e.g., show a confirmation message)
        } else {
            // Handle failure (e.g., show an error message)
        }
    }
}


@Composable
fun IconToggleButton(iconResId: Int, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .size(50.dp), // Adjust size as needed
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            modifier = Modifier.size(36.dp) // Adjust icon size as needed
        )
    }
}

data class LocationPoint(val latitude: Double, val longitude: Double)
val locationPoint: List<LocationPoint> = listOf(
    //LocationPoint(38.800988, -9.2285129), // Casal de Cambra
    LocationPoint(38.8100, -9.2285),      // North of Casal de Cambra (Further)
    LocationPoint(38.8009, -9.2150),      // East of Casal de Cambra (Further)
    LocationPoint(38.7900, -9.2285),      // South of Casal de Cambra (Further)
    LocationPoint(38.8009, -9.2420)       // West of Casal de Cambra (Further)
)