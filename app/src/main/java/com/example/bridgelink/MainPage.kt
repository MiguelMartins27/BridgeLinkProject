package com.example.bridgelink


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AddReaction
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.bridgelink.danger.area.DangerArea
import com.example.bridgelink.danger.area.DangerAreaRepository
import com.example.bridgelink.navigation.Screens
import com.example.bridgelink.post.office.PostOfficeRepository
import com.example.bridgelink.signals.Signal
import com.example.bridgelink.signals.SignalRepository
import com.example.bridgelink.utils.SharedViewModel
import com.example.bridgelink.weatherinfo.WeatherInfo
import com.example.bridgelink.weatherinfo.WeatherInfoRepository
import com.google.gson.JsonObject
import com.mapbox.geojson.Point
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
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
import kotlinx.coroutines.launch
import kotlin.math.pow


@Composable
fun MainPage(
    navController: NavController,
    modifier: Modifier = Modifier,
    sharedViewModel: SharedViewModel
) {
    val locationState = sharedViewModel.location.collectAsState()
    val (latitude, longitude) = locationState.value
    val mapViewportState = rememberMapViewportState()
    val mapView = remember { MapView(navController.context) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.navy_blue))
    ) {
        // Map rendering
        MapboxMap(
            Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
            style = { MapStyle(style = "mapbox://styles/miguelmartins27/cm4k61vj1007501si3wux1brp") }
        ) {
            MapEffect(Unit) { mapView ->
                mapView.location.updateSettings {
                    locationPuck = createDefault2DPuck(withBearing = true)
                    enabled = true
                    puckBearing = PuckBearing.COURSE
                    puckBearingEnabled = true
                }

                mapViewportState.transitionToFollowPuckState()

                // Load and manage layers
                addPostOfficesLayer(mapView)
                addTimefallLayer(mapView)
                addSignalsLayer(mapView)
                addChiralNetworkLayer(mapView)
            }
        }

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
        InteractionUtils(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = (56).dp), // Adjusted offset
            latitude = latitude,
            longitude = longitude
        )

        InteractionUtilsLayerControl(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = (-28).dp),
            mapView = mapView
        )
    }
}

@Composable
fun InteractionUtilsLayerControl(
    modifier: Modifier = Modifier,
    mapView: MapView
) {
    var isTimefallLayerEnabled by remember { mutableStateOf(true) }
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
        // Timefall Layer
        IconToggleButton(
            iconResId = R.drawable.umbrella,
            contentDescription = "Toggle Timefall Layer",
            onClick = {
                isTimefallLayerEnabled = !isTimefallLayerEnabled
                toggleAnnotation("Timefall", isTimefallLayerEnabled)
            }
        )
        Spacer(modifier = Modifier.width(8.dp))

        // Chiral Network Layer
        IconToggleButton(
            iconResId = R.drawable.connections,
            contentDescription = "Toggle Chiral Network Layer",
            onClick = {
                isChiralNetworkLayerEnabled = !isChiralNetworkLayerEnabled
                toggleAnnotation("ChiralNetwork", isChiralNetworkLayerEnabled)
            }
        )
        Spacer(modifier = Modifier.width(8.dp))

        // Signals Layer
        IconToggleButton(
            iconResId = R.drawable.signals,
            contentDescription = "Toggle Signals Layer",
            onClick = {
                isSignalsLayerEnabled = !isSignalsLayerEnabled
                toggleAnnotation("Signals", isSignalsLayerEnabled)
            }
        )
    }
}

@Composable
fun InteractionUtils(modifier: Modifier = Modifier, latitude: Double, longitude: Double) {
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
        // See connections
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

        // Add a Reaction
        Row(
            modifier = Modifier
                .padding(horizontal = 4.dp), // Reduced padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = {  }
            ) {
                Icon(
                    imageVector = Icons.Default.AddAlert,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(36.dp) // Reduced icon size
                        .padding(end = 4.dp)
                )
            }
        }

        // Ask for help
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp), // Reduced padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { }
            ) {
                Icon(
                    imageVector = Icons.Default.Bloodtype,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier
                        .size(36.dp) // Reduced icon size
                        .padding(end = 4.dp)
                )
            }
        }
    }
    if (showAddSignalDialog) {
        AddSignalDialog(
            onDismiss = { showAddSignalDialog = false },
            onAddSignal = { iconId, description ->
                // Handle adding the signal here
                showAddSignalDialog = false
            },
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
    onAddSignal: (String, String) -> Unit
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

private lateinit var timefallManager: CircleAnnotationManager
private val timefallAnnotations = mutableListOf<CircleAnnotation>()

fun addPostOfficesLayer(mapView: MapView) {
    val postOfficeRepository = PostOfficeRepository()
    postOfficeRepository.fetchPostOffices { postOffices ->
        mapView.getMapboxMap().getStyle { style ->
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
            mapView.getMapboxMap().addOnCameraChangeListener {
                val currentZoom = mapView.getMapboxMap().cameraState.zoom

                // Define the zoom range where the annotations should be visible
                val shouldBeVisible = currentZoom >= 14.0 && currentZoom <= 18.0

                // Update the opacity of annotations based on the zoom level
                postOfficeAnnotations.forEach { annotation ->
                    annotation.iconOpacity = if (shouldBeVisible) 1.0 else 0.0
                    postOfficeManager.update(annotation)
                }
            }
        }
    }
}

fun addChiralNetworkLayer(mapView: MapView) {
    val repository = DangerAreaRepository()
    repository.fetchDangerAreas { areas ->
        mapView.getMapboxMap().getStyle { style ->
            val annotationApi = mapView.annotations
            ChiralNetworkManager = annotationApi.createCircleAnnotationManager()

            areas.forEach { area ->
                val radiusInMeters = area.radius * 20
                val dangerArea = CircleAnnotationOptions()
                    .withPoint(Point.fromLngLat(area.longitude, area.latitude))
                    .withCircleOpacity(0.5)
                    .withCircleColor("#FF0000")
                    .withCircleRadius(radiusInMeters)

                val annotation = ChiralNetworkManager.create(dangerArea)
                ChiralNetworkAnnotations.add(annotation)
            }

            // Add zoom change listener
            mapView.getMapboxMap().addOnCameraChangeListener {
                val currentZoom = mapView.getMapboxMap().cameraState.zoom

                // Define the zoom range where the annotations should be visible
                val shouldBeVisible = currentZoom >= 14.0 && currentZoom <= 18.0

                // Update the opacity of annotations based on the zoom level
                ChiralNetworkAnnotations.forEach { annotation ->
                    annotation.circleOpacity = if (shouldBeVisible) 0.5 else 0.0
                    ChiralNetworkManager.update(annotation)
                }
                areas.forEach { area ->
                    val radiusInMeters = area.radius * 20
                    val circle = ChiralNetworkAnnotations.find { it.point.latitude() == area.latitude && it.point.longitude() == area.longitude }
                    if (circle != null) {
                        val newRadius = radiusInMeters * 2.0.pow(currentZoom - 14.0)
                        circle.circleRadius = newRadius
                        ChiralNetworkManager.update(circle)
                    }
                }

            }
        }
    }
}

fun addSignalsLayer(mapView: MapView) {
    val signalRepository = SignalRepository()
    signalRepository.fetchSignals { signals ->
        mapView.getMapboxMap().getStyle { style ->
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
            mapView.getMapboxMap().addOnCameraChangeListener {
                val currentZoom = mapView.getMapboxMap().cameraState.zoom

                // Define the zoom range where the annotations should be visible
                val shouldBeVisible = currentZoom >= 14.0 && currentZoom <= 18.0

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
    val locations = listOf("California", "Lisboa", "Tokyo")
    val locationKeys = mutableListOf<String>()
    val weatherData = mutableListOf<WeatherInfo>()

    val weatherInfoRepository = WeatherInfoRepository()

    // Step 1: Retrieve location keys
    CoroutineScope(Dispatchers.Main).launch {
        locations.forEach { location ->
            val locationKey = weatherInfoRepository.fetchLocationKey(location)
            if (locationKey != null) {
                locationKeys.add(locationKey)
            } else {
                Log.e("WeatherLayer", "Location key not found for: $location")
            }
        }
    }

    // Step 2: Fetch weather data for each location
    CoroutineScope(Dispatchers.Main).launch {
        locationKeys.forEach { locationKey ->
            val weather = locationKey.let { weatherInfoRepository.fetchWeatherData(it) }
            if (weather != null) {
                weatherData.add(weather)
            } else {
                Log.e("WeatherLayer", "Weather data not found for location key: $locationKey")
            }
        }
    }


    // Step 3: Add weather data to map
    mapView.getMapboxMap().getStyle { style ->
        val annotationApi = mapView.annotations
        timefallManager = annotationApi.createCircleAnnotationManager()

        // Use only the first weather point
        val point = Point.fromLngLat(-9.3, 38.79699)

        // Create the CircleAnnotationOptions
        val circleAnnotationOptions = CircleAnnotationOptions()
            .withPoint(point)
            .withCircleRadius(60.0) // Set the radius of the circle
            .withCircleColor("#FF5733") // Set the color of the circle (example: orange)
            .withCircleOpacity(0.7) // Set the opacity of the circle
            .withData(JsonObject().apply {
                // Use the description from the first weather data entry
                addProperty("description", weatherData.firstOrNull()?.description ?: "No description")
            })

        // Create the circle annotation and add it to the manager
        val circleAnnotation = timefallManager.create(circleAnnotationOptions)
        timefallAnnotations.add(circleAnnotation)
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