package com.example.bridgelink

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.bridgelink.navigation.NavGraph
import com.example.bridgelink.ui.theme.BridgeLinkTheme
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
    // Firebase instance variables
    private lateinit var auth: FirebaseAuth

    // Define the PermissionsListener for Mapbox location permissions
    private val permissionsListener: PermissionsListener = object : PermissionsListener {
        override fun onExplanationNeeded(permissionsToExplain: List<String>) {
            // This is where you can explain to the user why you need the permission
            // For example, show a message explaining the need for location access
            Toast.makeText(
                applicationContext,
                "Location permission is required for the map functionality",
                Toast.LENGTH_LONG
            ).show()
        }

        override fun onPermissionResult(granted: Boolean) {
            if (granted) {
                // Permission granted, proceed with location-sensitive logic
                Toast.makeText(applicationContext, "Location permission granted", Toast.LENGTH_SHORT).show()
                // Activate Maps SDK LocationComponent here, or any other logic
            } else {
                // Permission denied, handle this case
                Toast.makeText(applicationContext, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth and check if the user is signed in
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        mapView = MapView(this)
        if (this::mapView.isInitialized) {
            mapView.getMapboxMap().loadStyleUri("mapbox://styles/miguelmartins27/cm4k61vj1007501si3wux1brp") {
                enableLocationComponent()
            }
        }

        // Initialize Firebase
        // FirebaseApp.initializeApp(this)
        // Initialize PermissionsManager with the current activity context
        permissionsManager = PermissionsManager(permissionsListener)

        // Check if location permissions are granted
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Proceed with location-sensitive logic
            Toast.makeText(applicationContext, "Location permissions are granted", Toast.LENGTH_SHORT).show()
        } else {
            // Request the location permissions
            permissionsManager.requestLocationPermissions(this)
        }

        setContent {
            BridgeLinkTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        sharedViewModel = viewModel,
                        signOut = {
                            signOutUser()
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

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in.
        if (auth.currentUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }
    }

    private fun getPhotoUrl(): String? {
        val user = auth.currentUser
        return user?.photoUrl?.toString()
    }

    private fun getUserName(): String? {
        val user = auth.currentUser
        return if (user != null) {
            user.displayName
        } else ANONYMOUS
    }

    private fun signOutUser() {
        auth.signOut()
        // Navigate to SignInActivity after sign-out
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()  // Close MainActivity to avoid returning to it after sign-out
    }

    companion object {
        const val ANONYMOUS = "anonymous"
    }
}
