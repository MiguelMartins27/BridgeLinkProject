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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.bridgelink.navigation.Screens
import com.example.bridgelink.post.office.PostOfficeRepository
import com.example.bridgelink.signals.Signal
import com.example.bridgelink.signals.SignalRepository
import com.example.bridgelink.utils.SharedViewModel
import com.google.gson.JsonObject
import com.mapbox.geojson.Point
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.location
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


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
                val resizedBitmap = resizeDrawable(
                    context = mapView.context,
                    drawableId = R.drawable.porter,
                    width = 200,  // Set your desired width
                    height = 200  // Set your desired height
                )
                mapView.location.updateSettings {
                    locationPuck = LocationPuck2D(
                        topImage = ImageHolder.from(resizedBitmap)
                    )
                    enabled = true
                    puckBearing = PuckBearing.COURSE
                    puckBearingEnabled = true
                }

                mapViewportState.transitionToFollowPuckState()

                // Load and manage layers
                addPostOfficesLayer(mapView)
                addTimefallLayer(mapView)
                addChiralNetworkLayer(mapView)
                addSignalsLayer(mapView, SignalRepository().signalIcons)
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

fun addPostOfficesLayer(mapView: MapView) {
    val postOfficeRepository = PostOfficeRepository()
    postOfficeRepository.fetchPostOffices { postOffices ->
        mapView.getMapboxMap().getStyle { style ->
            // Create an instance of the Annotation API and get the PointAnnotationManager
            val annotationApi = mapView.annotations
            val pointAnnotationManager = annotationApi.createPointAnnotationManager()

            // Create a bitmap for the marker icon
            val originalBitmap = BitmapFactory.decodeResource(mapView.context.resources, R.drawable.postbox)
            val width = originalBitmap.width / 8 // Reduce width by half
            val height = originalBitmap.height / 8 // Reduce height by half
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false)

            postOffices.forEach { postOffice ->
                // Set options for the resulting point annotation
                val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                    // Define a geographic coordinate
                    .withPoint(Point.fromLngLat(postOffice.longitude, postOffice.latitude))
                    // Specify the bitmap you assigned to the point annotation
                    .withIconImage(scaledBitmap)
                    // Add the post office name as data to the annotation
                    .withData(JsonObject().apply {
                        addProperty("name", postOffice.name)
                    })

                // Add the resulting pointAnnotation to the map
                pointAnnotationManager.create(pointAnnotationOptions)
            }
        }
    }
}

fun addTimefallLayer(mapView: MapView) {
    // Add logic to show dynamic timefall regions.
    // Change region colors based on timefall intensity.
}

fun addChiralNetworkLayer(mapView: MapView) {
    // Add logic to show dynamic chiral network regions.
    // Change region colors based on delivery activity.
}

fun addSignalsLayer(mapView: MapView, signals: List<Int>) {
    val signalRepository = SignalRepository()
    signalRepository.fetchSignals { signals ->
        mapView.getMapboxMap().getStyle { style ->
            val annotationApi = mapView.annotations
            val pointAnnotationManager = annotationApi.createPointAnnotationManager()

            signals.forEach { signal ->
                val iconResourceId = signal.iconResourceName.toInt()
                if (iconResourceId != 0) { // Check if resource is found
                    val originalBitmap = BitmapFactory.decodeResource(mapView.context.resources, iconResourceId)
                    val width = originalBitmap.width / 8 // Reduce width by half
                    val height = originalBitmap.height / 8 // Reduce height by half
                    val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false)
                    val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                        .withPoint(Point.fromLngLat(signal.longitude, signal.latitude))
                        .withIconImage(scaledBitmap)
                        .withData(JsonObject().apply {
                            addProperty("description", signal.description)
                        })

                    pointAnnotationManager.create(pointAnnotationOptions)
                } else {
                    Log.e("MyTag", "Icon resource not found for: ${signal.iconResourceName}")
                }
            }
        }
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
                onClick = { openContacts() }
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
                onClick = { requestHelp() }
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
fun InteractionUtilsLayerControl(
    modifier: Modifier = Modifier,
    mapView: MapView
) {
    val dangerAreas = remember { mutableStateListOf<Pair<Double, Double>>() }

    Row(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 0.dp,
                    bottomEnd = 0.dp,
                    bottomStart = 16.dp
                )
            ) // Slightly smaller corner radius
            .background(Color(0xE64682B4))
            .height(64.dp) // Adjusted height for horizontal layout
            .padding(horizontal = 8.dp), // Added horizontal padding
        horizontalArrangement = Arrangement.Center, // Centering the buttons horizontally
        verticalAlignment = Alignment.CenterVertically // Aligning the buttons vertically
    ) {
        // UI Buttons with logos to toggle layers
        IconToggleButton(
            iconResId = R.drawable.umbrella,
            contentDescription = "Toggle Timefall Layer",
            onClick = { toggleTimefallLayer() }
        )
        Spacer(modifier = Modifier.width(8.dp)) // Spacer between buttons
        IconToggleButton(
            iconResId = R.drawable.connections,
            contentDescription = "Toggle Chiral Network Layer",
            onClick = { toggleChiralNetworkLayer(mapView, dangerAreas) }
        )
        Spacer(modifier = Modifier.width(8.dp)) // Spacer between buttons
        IconToggleButton(
            iconResId = R.drawable.signals,
            contentDescription = "Toggle Signals Layer",
            onClick = { toggleSignalsLayer() }
        )
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

fun toggleTimefallLayer() {
    // Toggle the visibility of the Timefall layer.
}

fun toggleChiralNetworkLayer(mapView: MapView, dangerAreas: MutableList<Pair<Double, Double>>) {
    // Define danger areas close to the user's starting position
    val predefinedDangerAreas = listOf(
        Pair(38.796801, -9.233749), // Slightly north
        Pair(38.796601, -9.233849), // Slightly south
        Pair(38.796701, -9.233649), // Slightly east
        Pair(38.796701, -9.233849)  // Slightly west
    )

    // Add these areas to the dangerAreas list
    dangerAreas.clear() // Clear previous areas to avoid adding the same ones multiple times
    dangerAreas.addAll(predefinedDangerAreas)

    // Log the danger areas added
    println("Danger areas added: $dangerAreas")

    // Set up these danger areas on the map
    setupDangerAreas(mapView, dangerAreas, radius = 5000.0)
}

// Function to set up multiple danger areas
fun setupDangerAreas(mapView: MapView, dangerAreas: List<Pair<Double, Double>>, radius: Double) {
    val annotationApi = mapView.annotations
    val circleAnnotationManager = annotationApi.createCircleAnnotationManager()

    // Convert the radius to a pixel-based size (assumes a fixed zoom level, adjust as needed)
    val pixelRadius = radius.toFloat() // This controls the radius of the circle
    println("Using radius: $radius meters, which converts to $pixelRadius pixels")

    dangerAreas.forEachIndexed { index, area ->
        println("Creating circle for danger area $index at latitude: ${area.first}, longitude: ${area.second}")
        val dangerArea = CircleAnnotationOptions()
            .withPoint(Point.fromLngLat(area.second, area.first)) // Longitude first, then latitude
            .withCircleRadius(pixelRadius.toDouble())
            .withCircleColor("#FF0000")
            .withCircleOpacity(1.0)

        // Log before creating each circle annotation
        println("Creating circle annotation with options: $dangerArea")

        try {
            circleAnnotationManager.create(dangerArea)
            println("Successfully created danger area circle at (${area.first}, ${area.second})")
        } catch (e: Exception) {
            println("Error creating circle annotation for danger area $index at (${area.first}, ${area.second}): ${e.message}")
        }
    }
}

// Function to add a danger area
fun addDangerArea(mapView: MapView, latitude: Double, longitude: Double, radius: Double) {
    val annotationApi = mapView.annotations
    val circleAnnotationManager = annotationApi.createCircleAnnotationManager()

    val dangerArea = CircleAnnotationOptions()
        .withPoint(Point.fromLngLat(longitude, latitude))
        .withCircleRadius(radius)
        .withCircleColor("#FF0000")
        .withCircleOpacity(0.5)

    // Log the danger area details
    println("Adding danger area at latitude: $latitude, longitude: $longitude with radius: $radius meters")

    try {
        circleAnnotationManager.create(dangerArea)
        println("Successfully added danger area at ($latitude, $longitude)")
    } catch (e: Exception) {
        println("Error adding danger area at ($latitude, $longitude): ${e.message}")
    }
}

// Function to check proximity to danger areas
fun checkProximityToDangerAreas(
    userLat: Double,
    userLng: Double,
    dangerAreas: List<Pair<Double, Double>>,
    radius: Double
): String {
    for ((index, area) in dangerAreas.withIndex()) {
        val distance = calculateDistance(userLat, userLng, area.first, area.second)
        println("Checking proximity for user at ($userLat, $userLng) to danger area $index at (${area.first}, ${area.second}) with distance $distance meters")

        if (distance <= radius) {
            return if (distance < 50) "Danger" else "Unsafe"
        }
    }
    return "Safe"
}

// Utility function to calculate distance between two geographic points in meters
fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadius = 6371000.0 // meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val distance = earthRadius * c

    // Debugging the distance calculation
    println("Calculated distance between ($lat1, $lng1) and ($lat2, $lng2) is $distance meters")
    return distance
}

fun toggleSignalsLayer() {
    // Toggle the visibility of the Signals layer.
}


// TODO Lógica de pedir ajuda de outros porters
// Isso inclui enviar o pedido para porters nas proximidades; Criar um pop-up para estes;
// Adicionar um icone ao mapa no local do porter que precisa de ajuda;
// Apagar este icone e eliminar o pedido quando alguem se aproxima dentro de 50m (por exemplo)
fun requestHelp() {
    print("Requesting for help")
}

// TODO Lógica de contactos
// Criar uma página que pode aparecer por cima desta com os porters e a distância a que estão
fun openContacts() {
    print("List of contacts")
}

fun resizeDrawable(context: Context, drawableId: Int, width: Int, height: Int): Bitmap {
    val drawable: Drawable = ContextCompat.getDrawable(context, drawableId)!!
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
