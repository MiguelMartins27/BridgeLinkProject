package com.example.bridgelink

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.bridgelink.navigation.NavGraph
import com.example.bridgelink.ui.theme.BridgeLinkTheme
import com.example.bridgelink.utils.RouteViewModel
import com.example.bridgelink.utils.SharedViewModel
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.location


class MainActivity : ComponentActivity() {

    private lateinit var permissionsManager: PermissionsManager
    private lateinit var mapView: MapView
    var longitude: Double = 0.0
    var latitude: Double = 0.0
    private val viewModel: SharedViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var captureImageLauncher: ActivityResultLauncher<Uri>
    private lateinit var imageUri: Uri
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001

    // Define the PermissionsListener for Mapbox location permissions
    private val permissionsListener: PermissionsListener = object : PermissionsListener {
        override fun onExplanationNeeded(permissionsToExplain: List<String>) {
            Toast.makeText(
                applicationContext,
                "Location permission is required for the map functionality",
                Toast.LENGTH_LONG
            ).show()
        }

        override fun onPermissionResult(granted: Boolean) {
            if (granted) {
                Toast.makeText(applicationContext, "Location permission granted", Toast.LENGTH_SHORT).show()
                enableLocationComponent()
            } else {
                Toast.makeText(applicationContext, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
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

        // Initialize MapView
        mapView = MapView(this)
        setContentView(mapView)  // Add map view to layout

        // Load the map style
        mapView.getMapboxMap().loadStyleUri("mapbox://styles/miguelmartins27/cm4k61vj1007501si3wux1brp") {
            enableLocationComponent()
        }

        // Initialize the permissions manager
        permissionsManager = PermissionsManager(permissionsListener)

        // Check for location permissions and request if necessary
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            enableLocationComponent()
        } else {
            permissionsManager.requestLocationPermissions(this)
        }

        // Handle camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            Toast.makeText(applicationContext, "Camera permission already granted", Toast.LENGTH_SHORT).show()
        }

        // Set up content using Jetpack Compose
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
                        signOut = { signOutUser()
                        }
                    )
                }
            }
        }
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
        }
    }

    // Handle the result of the camera permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Camera permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    private fun signOutUser() {
        auth.signOut()
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }



    companion object {
        const val ANONYMOUS = "anonymous"
    }
}
