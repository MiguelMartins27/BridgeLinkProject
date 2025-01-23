package com.example.bridgelink

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat

import androidx.navigation.compose.rememberNavController
import com.example.bridgelink.navigation.NavGraph
import com.example.bridgelink.ui.theme.BridgeLinkTheme
import com.example.bridgelink.utils.RouteViewModel
import com.example.bridgelink.utils.SharedViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.geojson.Point

class MainActivity : ComponentActivity() {

    private lateinit var permissionsManager: PermissionsManager
    private lateinit var mapView: MapView
    var longitude: Double = 0.0
    var latitude: Double = 0.0
    private val viewModel: SharedViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private var lastLocation: Location? = null
    private var lastLocationTime = System.currentTimeMillis()
    private val velocityThreshold = 5.0 // meters per second (adjust as needed)
    private val dangerAreas = mutableListOf<Point>()

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
                enableLocationComponent()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth and check if the user is signed in
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        // Initialize MapView properly
        mapView = MapView(this)
        setContentView(mapView)

        mapView.getMapboxMap().loadStyleUri("mapbox://styles/miguelmartins27/cm4k61vj1007501si3wux1brp") {
            enableLocationComponent()
        }

        // Request location permissions
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableLocationComponent()
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Request camera permission
        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)

        // Set up the Compose UI
        setContent {
            BridgeLinkTheme {
                val navController = rememberNavController()
                val routeViewModel: RouteViewModel = viewModels<RouteViewModel>().value
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        sharedViewModel = viewModel,
                        routeViewModel = routeViewModel,
                        signOut = { signOutUser() }
                    )
                }
            }
        }

        // Fetch danger areas
        addWeatherBasedDangerLayer()
    }



    private fun enableLocationComponent() {
        val locationComponentPlugin: LocationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            enabled = true
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener { point ->
            latitude = point.latitude()
            longitude = point.longitude()
            viewModel.updateLocation(latitude, longitude)

            // Calculate velocity and check danger area
            onLocationUpdated(latitude, longitude)
        }
    }

    private fun onLocationUpdated(latitude: Double, longitude: Double) {
        val currentLocation = Location("current")
        currentLocation.latitude = latitude
        currentLocation.longitude = longitude

        lastLocation?.let {
            val distance = it.distanceTo(currentLocation)
            val timeDifference = (System.currentTimeMillis() - lastLocationTime) / 1000.0 // seconds
            val velocity = if (timeDifference > 0) distance / timeDifference else 0.0

            // Check if velocity exceeds the threshold
            if (velocity > velocityThreshold) {
                checkForDangerZone(latitude, longitude)
            }
        }

        // Update the last location and time
        lastLocation = currentLocation
        lastLocationTime = System.currentTimeMillis()
    }

    private fun checkForDangerZone(latitude: Double, longitude: Double) {
        // Check if the current location is within any of the danger areas
        dangerAreas.forEach { dangerPoint ->
            val distanceToDangerArea = FloatArray(1)
            Location.distanceBetween(latitude, longitude, dangerPoint.latitude(), dangerPoint.longitude(), distanceToDangerArea)

            // If within the danger zone, trigger vibration
            if (distanceToDangerArea[0] <= 200) { // Within 200 meters
                triggerVibration()
            }
        }
    }

    private fun triggerVibration() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            // Vibrate for 1 second (1000 milliseconds)
            vibrator.vibrate(2000)
        } else {
            Log.d("VibrationTest", "Device does not have a vibrator.")
        }
    }

    private fun addWeatherBasedDangerLayer() {
        // Example of how you can get danger areas from Firebase or define static areas
        val weatherRef = FirebaseDatabase.getInstance().reference.child("weatherCurrentInApp")

        weatherRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear existing danger areas
                dangerAreas.clear()

                snapshot.children.forEach { locationSnapshot ->
                    val condition = locationSnapshot.child("condition").getValue(String::class.java) ?: ""
                    val latitude = locationSnapshot.child("latitude").getValue(Double::class.java) ?: 0.0
                    val longitude = locationSnapshot.child("longitude").getValue(Double::class.java) ?: 0.0

                    // Check if the condition contains "rain", and consider it a danger zone
                    if (condition.contains("rain", ignoreCase = true) ||
                        condition.contains("showers", ignoreCase = true)
                    ) {
                        dangerAreas.add(Point.fromLngLat(longitude, latitude))
                        Log.d("DangerLayer", "Danger area added at $latitude, $longitude for condition: $condition")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DangerLayer", "Error fetching weather data: ${error.message}")
            }
        })
    }

    private fun signOutUser() {
        auth.signOut()
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

}
