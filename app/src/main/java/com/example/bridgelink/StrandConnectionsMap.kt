package com.example.bridgelink

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddReaction
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bridgelink.navigation.NavigationDots
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location


@Composable
fun StrandConnectionsMap(navController: NavController, modifier: Modifier = Modifier) {
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
                    locationPuck = createDefault2DPuck(withBearing = true)
                    enabled = true
                    puckBearing = PuckBearing.COURSE
                    puckBearingEnabled = true
                }
                mapViewportState.transitionToFollowPuckState()
            }
        }

        // Interaction menu overlapping the map on the right side
        InteractionUtils(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 300.dp)
        )

        // Navigation dots at the bottom
        NavigationDots(
            currentPage = 3,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        )
    }
}


@Preview
@Composable
fun InteractionUtils(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(72.dp)
            .clip(shape = RoundedCornerShape(20.dp))
            .background(Color(0xE64682B4))
            .height(300.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // See connections
        Row(
            modifier = Modifier
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Contacts,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 4.dp)
            )
        }

        // Add a Reaction
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = {AddReaction()}
            ) {
                Icon(
                    imageVector = Icons.Default.AddReaction,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(60.dp)
                        .padding(end = 4.dp)
                )
            }
        }

        // Ask for help
        Row(
            modifier = Modifier
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = {RequestHelp()}
            ) {
                Icon(
                    imageVector = Icons.Default.Bloodtype,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier
                        .size(45.dp)
                        .padding(end = 4.dp)
                )
            }

        }
    }
}

// TODO Lógica de adicionar icones
// Isso incluir adicionar as layers ao mapa; Persistir os ícones na db;
// Ter layering para que não se vejam todos os icones at all times
fun AddReaction() {
    print("Add reaction")
}

// TODO Lógica de pedir ajuda de outros porters
// Isso inclui enviar o pedido para porters nas proximidades; Criar um pop-up para estes;
// Adicionar um icone ao mapa no local do porter que precisa de ajuda;
// Apagar este icone e eliminar o pedido quando alguem se aproxima dentro de 50m (por exemplo)
fun RequestHelp() {
    print("Requesting for help")
}

// TODO Lógica de contactos
// Criar uma página que pode aparecer por cima desta com os porters e a distância a que estão
fun OpenContacts() {
    print("List of contacts")
}