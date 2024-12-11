package com.example.bridgelink

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.bridgelink.navigation.NavGraph
import com.example.bridgelink.ui.theme.BridgeLinkTheme
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.core.permissions.PermissionsListener

class MainActivity : ComponentActivity() {

    private lateinit var permissionsManager: PermissionsManager

    // Define the PermissionsListener
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
                Toast.makeText(applicationContext, "Permission granted", Toast.LENGTH_SHORT).show()
                // Activate Maps SDK LocationComponent here, or any other logic
            } else {
                // Permission denied, handle this case
                Toast.makeText(applicationContext, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize PermissionsManager with the current activity context
        permissionsManager = PermissionsManager(permissionsListener)

        // Check if location permissions are granted
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Permission granted, proceed with the logic (e.g., showing the user's location)
            Toast.makeText(applicationContext, "Location permission already granted", Toast.LENGTH_SHORT).show()
            // Add any logic to activate Mapbox's LocationComponent or other location-sensitive logic
        } else {
            // Request the location permissions
            permissionsManager.requestLocationPermissions(this)
        }

        setContent {
            BridgeLinkTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Only include the NavGraph
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    // Override to handle the permission request result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
