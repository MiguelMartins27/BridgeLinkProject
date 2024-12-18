package com.example.bridgelink

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import com.example.bridgelink.navigation.Screens

@Composable
fun Menu (navController: NavController, modifier: Modifier = Modifier) {
    val mapViewportState = rememberMapViewportState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.navy_blue))
    ) {
        Text(
            text = "BridgeLink",
            color = Color.White,
            fontSize = 24.sp,
            modifier = Modifier.padding(16.dp)
                .align(Alignment.CenterHorizontally)
        )

        // Map view - assuming you have a custom composable for your map
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
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
                }
            }
        }

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActionButton(
                text = "Odradek Scanner",
                icon = Icons.Default.Scanner,
                navController = navController
            )
            ActionButton(
                text = "Check Weather",
                icon = Icons.Default.Cloud,
                navController = navController
            )
            ActionButton(
                text = "Manage Cargo",
                icon = Icons.Default.Backpack,
                navController = navController
            )
        }

        // Alert section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                colorResource(id = R.color.yellow)
            )
        ) {
            Text(
                text = "ALERT: Timefall Warning!\nHeavy Timefall expected in your area.",
                modifier = Modifier.padding(8.dp),
                color = Color.Black
            )
        }

        // Community updates
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                colorResource(id = R.color.home_grey)
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "Community Updates",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                LazyColumn {
                    items(communityUpdates) { update ->
                        Text(
                            text = "• $update",
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    navController: NavController,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable {
                when (text) {
                    "Odradek Scanner" -> navController.navigate(Screens.OdradekScanner.route)
                    "Check Weather" -> navController.navigate(Screens.TimeFallForecast.route)
                    "Manage Cargo" -> navController.navigate(Screens.CargoManagement.route)
                }
            }
            .background(colorResource(id = R.color.black))
            .clip(RoundedCornerShape(16.dp))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White
        )
        Text(
            text = text,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = 12.sp
        )
    }
}

private val communityUpdates = listOf(
    "Sam Porter: 'Bridge completed at coordina...'",
    "Fragile: 'BTs spotted near Mountain Kn...'",
    "Deadman: 'New equipment available at'"
)