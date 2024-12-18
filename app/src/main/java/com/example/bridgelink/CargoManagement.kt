package com.example.bridgelink

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mapbox.maps.extension.style.expressions.dsl.generated.color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CargoManagement(navController: NavController, modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.navy_blue))
    ) {
        TopAppBar(
            title = { Text(text = "Cargo Management", color = colorResource(id = R.color.white)) },
            navigationIcon = {
                IconButton(onClick = { /* Handle navigation */ }) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            },
            modifier = Modifier.background(colorResource(id = R.color.navy_blue))
        )

        // Current Cargo Overview Section
        Text(
            text = "Current Cargo Overview",
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Text("Total Weight: 120 kg")
        Text("Packages: 8")

        Row(modifier = Modifier.padding(vertical = 4.dp)) {
            Text("Condition: ")
            repeat(3) {
                Icon(
                    Icons.Filled.Circle,
                    contentDescription = null,
                    tint = colorResource(id = R.color.white)
                )
            }
            repeat(2) {
                Icon(
                    Icons.Filled.Circle,
                    contentDescription = null,
                    tint = colorResource(id = R.color.yellow)
                )
            }
            Text(" (3/5)")
        }

        // Cargo Distribution View
        Text(
            text = "Cargo Distribution Recommendation View",
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Distribution Grid (simplified version)
        CargoDistributionGrid()

        Text("Balance:", modifier = Modifier.padding(vertical = 8.dp))
        LinearProgressIndicator(
            progress = 0.5f,
            modifier = Modifier.fillMaxWidth()
        )

        // Cargo List
        LazyColumn (
            modifier = Modifier.weight(1f),
        ) {
            val cargoItems =
                listOf(
                    CargoData("Package 1", 10, "City 1", 3),
                    CargoData("Package 2", 20, "City 2", 2),
                    CargoData("Package 3", 30, "City 3", 1),
                    CargoData("Package 4", 40, "City 4", 3),
                    CargoData("Package 5", 50, "City 5", 2),
                    CargoData("Package 6", 60, "City 6", 1),
                    CargoData("Package 7", 70, "City 7", 3),
                    CargoData("Package 8", 80, "City 8", 2)
                )
            items(cargoItems) { item ->
                CargoItem(item)
            }
        }

        // Bottom Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { /* Handle optimize */ }) {
                Text("Optimize Load")
            }
            Button(onClick = { /* Handle add */ }) {
                Text("Add Cargo")
            }
            Button(onClick = { /* Handle remove */ }) {
                Text("Remove Cargo")
            }
        }
    }
}

@Composable
fun CargoItem(cargo: CargoData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.AddBox, contentDescription = null)
        Column(modifier = Modifier.weight(1f)) {
            Text(cargo.name)
            Text(
                "To: ${cargo.destination}",
            )
        }
        Text("${cargo.weight} kg")
        Row {
            repeat(cargo.condition) {
                Icon(
                    Icons.Filled.Circle,
                    contentDescription = null,
                    tint = colorResource(id = R.color.yellow)
                )
            }
        }
    }
}

data class CargoData(
    val name: String,
    val weight: Int,
    val destination: String,
    val condition: Int
)

@Composable
fun CargoDistributionGrid() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {

    }
}
