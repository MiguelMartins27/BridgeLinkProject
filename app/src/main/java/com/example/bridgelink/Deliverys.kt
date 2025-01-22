package com.example.bridgelink

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.bridgelink.deliveries.DeliveryRepository
import com.example.bridgelink.deliviries.Delivery
import androidx.compose.runtime.*
import androidx.compose.ui.res.colorResource
import androidx.navigation.NavController
import com.example.bridgelink.navigation.Screens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveriesScreen(navController: NavController) {
    val deliveryRepository = remember { DeliveryRepository() }
    val deliveriesFlow = deliveryRepository.fetchDeliveries()
    val deliveries by deliveriesFlow.collectAsState(initial = emptyList())

    // Coroutine scope for launching the suspend function
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Back Button in TopBar
            TopAppBar(
                title = { Text("My Deliveries", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Screens.SecondMainPage.route) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(colorResource(id = R.color.navy_blue))
        ) {
            // Group deliveries into ongoing and completed
            val ongoingDeliveries = deliveries.filter { !it.delivered }
            val completedDeliveries = deliveries.filter { it.delivered }

            // LazyColumn to display ongoing and completed deliveries
            Column(modifier = Modifier.fillMaxSize().padding(bottom = 16.dp)) {
                if (ongoingDeliveries.isNotEmpty()) {
                    Text(
                        "Ongoing Deliveries",
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    LazyColumn {
                        items(ongoingDeliveries) { delivery ->
                            DeliveryCard(
                                delivery = delivery,
                                onComplete = {
                                    coroutineScope.launch {
                                        val success = deliveryRepository.markDeliveryAsComplete(delivery)
                                        if (success) {
                                            // Optionally show a success message or UI update
                                        } else {
                                            // Handle failure (e.g., show a toast)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                if (completedDeliveries.isNotEmpty()) {
                    Text(
                        "Completed Deliveries",
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    LazyColumn {
                        items(completedDeliveries) { delivery ->
                            DeliveryCard(delivery = delivery, onComplete = {})
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeliveryCard(delivery: Delivery, onComplete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Display delivery image
            Image(
                painter = rememberImagePainter(delivery.imageUri),
                contentDescription = "Delivery Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Display delivery details
            Text("Weight: ${delivery.weight} kg", color = Color.White)
            Text("Size: ${delivery.size}", color = Color.White)
            Text("Condition: ${delivery.condition}", color = Color.White)
            Text("Fragile: ${if (delivery.fragile) "Yes" else "No"}", color = Color.White)

            // Display button only for ongoing deliveries
            if (!delivery.delivered) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Mark as Complete")
                }
            } else {
                Text(
                    "Delivery Completed",
                    color = Color.Green,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
