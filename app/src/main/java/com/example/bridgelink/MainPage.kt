package com.example.bridgelink


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddReaction
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bridgelink.navigation.Screens
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.geojson.GeoJson


@Composable
fun MainPage(navController: NavController, modifier: Modifier = Modifier) {
    val mapViewportState = rememberMapViewportState()
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
                    locationPuck = LocationPuck2D(
                        topImage = ImageHolder.from(R.drawable.eliseu),
                        bearingImage = ImageHolder.from(R.drawable.eliseu),
                        shadowImage = ImageHolder.from(R.drawable.eliseu)
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
                addSignalsLayer(mapView)
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
                .offset(y = (56).dp) // Adjusted offset
        )

        InteractionUtilsLayerControl(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = (-28).dp) // Adjusted offset
        )
    }
}

fun addPostOfficesLayer(mapView: MapView) {
    // Add logic to display post offices on the map.
    // This layer should be always visible.
}

fun addTimefallLayer(mapView: MapView) {
    // Add logic to show dynamic timefall regions.
    // Change region colors based on timefall intensity.
}


fun addChiralNetworkLayer(mapView: MapView) {
    // Add logic to show dynamic chiral network regions.
    // Change region colors based on delivery activity.
}

fun addSignalsLayer(mapView: MapView) {
    // Add logic to allow users to place and view signals.
    // Make this layer interactive.
}

@Preview
@Composable
fun InteractionUtils(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(56.dp) // Reduced width
            .clip(RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 0.dp,
                bottomEnd = 0.dp,
                bottomStart = 16.dp
            )) // Slightly smaller corner radius
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
                onClick = { openContacts() }
            ) {
                Icon(
                    imageVector = Icons.Default.Contacts,
                    contentDescription = null,
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
                onClick = { addReaction() }
            ) {
                Icon(
                    imageVector = Icons.Default.AddReaction,
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
}

@Composable
fun InteractionUtilsLayerControl(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 0.dp,
                bottomEnd = 0.dp,
                bottomStart = 16.dp
            )) // Slightly smaller corner radius
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
            onClick = { toggleChiralNetworkLayer() }
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

fun toggleChiralNetworkLayer() {
    // Toggle the visibility of the Chiral Network layer.
}

fun toggleSignalsLayer() {
    // Toggle the visibility of the Signals layer.
}

// TODO Lógica de adicionar icones
// Isso incluir adicionar as layers ao mapa; Persistir os ícones na db;
// Ter layering para que não se vejam todos os icones at all times
fun addReaction() {
    print("Add reaction")
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