package com.example.bridgelink

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.bridgelink.navigation.Screens
import com.example.bridgelink.utils.RouteViewModel
import com.example.bridgelink.weatherinfo.WeatherInfoRepository
import com.google.android.gms.maps.model.LatLng
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.compose.*
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location


@Composable
fun TraceRoute(navController: NavController, routeViewModel: RouteViewModel) {
    val mapViewportState = rememberMapViewportState { MapViewportState() }
    val points = remember { mutableStateListOf<LatLng>() }

    MapboxMap(
        Modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
        style = { MapStyle(style = "mapbox://styles/miguelmartins27/cm4k61vj1007501si3wux1brp") }
    ) {
        MapEffect(Unit) { mapView ->
            mapView.location.updateSettings {
                enabled = true
                pulsingEnabled = true
            }

            mapViewportState.transitionToFollowPuckState()

            addPostOfficesLayer(mapView)
            addTimefallLayer(mapView)
            addSignalsLayer(mapView)
            addWeatherBasedDangerLayer(mapView)

            mapView.gestures.addOnMapClickListener { point ->
                val latLng = LatLng(point.latitude(), point.longitude())
                points.add(latLng)
                mapView.getMapboxMap().getStyle()?.let { style ->
                    addMarkerToStyle(style, latLng)
                    if (points.size > 1) {
                        drawLineBetweenPoints(style, points)
                    }
                }
                true
            }
        }
    }

    Button(onClick = {
        // Add the current route to the ViewModel
        routeViewModel.addRoute(points)
        navController.navigate(Screens.MainPage.route)
    }) {
        Text("Finish Route")
    }
}


fun drawLineBetweenPoints(style: Style, points: List<LatLng>) {
    val lineString = LineString.fromLngLats(points.map { Point.fromLngLat(it.longitude, it.latitude) })
    style.styleSourceExists("line-source").let { exists ->
        if (!exists) {
            style.addSource(geoJsonSource("line-source") {
                geometry(lineString)
            })
            style.addLayer(lineLayer("line-layer", "line-source") {
                lineColor("#0000FF") // RED
                lineWidth(5.0)
            })
        } else {
            style.getSourceAs<GeoJsonSource>("line-source")?.geometry(lineString)
        }
    }
}


fun addMarkerToStyle(style: Style, latLng: LatLng) {
    val sourceId = "marker-source"
    val layerId = "marker-layer"

    style.styleSourceExists(sourceId).let { exists ->
        if (!exists) {
            style.addSource(geoJsonSource(sourceId) {
                feature(Feature.fromGeometry(Point.fromLngLat(latLng.longitude, latLng.latitude)))
            })
            style.addLayer(symbolLayer(layerId, sourceId) {
                iconImage("marker-icon")
                iconSize(1.0)
                iconColor("#FF0000") // Use a valid hex color format
                iconAnchor(IconAnchor.BOTTOM)
            })
        } else {
            val source = style.getSourceAs<GeoJsonSource>(sourceId)
            source?.let {
                val newFeature = Feature.fromGeometry(Point.fromLngLat(latLng.longitude, latLng.latitude))
                it.featureCollection(FeatureCollection.fromFeatures(listOf(newFeature)))
            }
        }
    }
}