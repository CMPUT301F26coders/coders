package com.example.coders.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private const val MAP_STYLE_URL =
    "https://tiles.openfreemap.org/styles/liberty"

@Composable
fun CampsiteMapPicker(
    modifier: Modifier = Modifier,
    initialLatitude: Double? = null,
    initialLongitude: Double? = null,
    interactive: Boolean = true,
    onLocationSelected: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember(context) {
        MapLibre.getInstance(context.applicationContext)

        MapView(context).apply {
            onCreate(null)
        }
    }

    var mapLibreMap by remember {
        mutableStateOf<MapLibreMap?>(null)
    }

    var selectedMarker by remember {
        mutableStateOf<Marker?>(null)
    }

    var mapReady by remember {
        mutableStateOf(false)
    }

    val latestCallback by rememberUpdatedState(onLocationSelected)
    val latestInteractive by rememberUpdatedState(interactive)

    fun moveMap(x: Float, y: Float) {
        mapLibreMap?.scrollBy(-x, -y, 250L)
    }

    fun zoomIn() {
        mapLibreMap?.animateCamera(
            CameraUpdateFactory.zoomIn()
        )
    }

    fun zoomOut() {
        mapLibreMap?.animateCamera(
            CameraUpdateFactory.zoomOut()
        )
    }

    Box(modifier = modifier) {

        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(
                alpha = 0.95f
            ),
            tonalElevation = 4.dp
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(4.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = { zoomIn() },
                        modifier = Modifier.semantics {
                            contentDescription = "Zoom in"
                        }
                    ) {
                        Text(
                            text = "+",
                            fontSize = 26.sp
                        )
                    }

                    IconButton(
                        onClick = { zoomOut() },
                        modifier = Modifier.semantics {
                            contentDescription = "Zoom out"
                        }
                    ) {
                        Text(
                            text = "−",
                            fontSize = 26.sp
                        )
                    }
                }

                IconButton(
                    onClick = {
                        moveMap(0f, -120f)
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "Move map up"
                    }
                ) {
                    Text(
                        text = "↑",
                        fontSize = 24.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = {
                            moveMap(-120f, 0f)
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Move map left"
                        }
                    ) {
                        Text(
                            text = "←",
                            fontSize = 24.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            moveMap(120f, 0f)
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Move map right"
                        }
                    ) {
                        Text(
                            text = "→",
                            fontSize = 24.sp
                        )
                    }
                }

                IconButton(
                    onClick = {
                        moveMap(0f, 120f)
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "Move map down"
                    }
                ) {
                    Text(
                        text = "↓",
                        fontSize = 24.sp
                    )
                }
            }
        }
    }

    DisposableEffect(mapView, lifecycleOwner) {

        val observer = LifecycleEventObserver { _, event ->

            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)

            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(mapView) {

        mapView.getMapAsync { map ->

            map.setStyle(
                Style.Builder().fromUri(MAP_STYLE_URL)
            ) {

                map.uiSettings.isAttributionEnabled = true

                map.uiSettings.isZoomGesturesEnabled = true
                map.uiSettings.isScrollGesturesEnabled = true

                map.uiSettings.isRotateGesturesEnabled = false

                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(53.5461, -113.4938),
                        10.0
                    )
                )

                map.addOnMapClickListener { point ->

                    if (latestInteractive) {

                        latestCallback(
                            point.latitude,
                            point.longitude
                        )

                        true

                    } else {
                        false
                    }
                }

                mapLibreMap = map
                mapReady = true
            }
        }
    }

    LaunchedEffect(
        mapReady,
        initialLatitude,
        initialLongitude
    ) {

        val map = mapLibreMap ?: return@LaunchedEffect

        if (!mapReady) return@LaunchedEffect

        val latitude = initialLatitude
        val longitude = initialLongitude

        if (
            latitude == null ||
            longitude == null ||
            latitude !in -90.0..90.0 ||
            longitude !in -180.0..180.0
        ) {

            selectedMarker?.let {
                map.removeMarker(it)
            }

            selectedMarker = null

            return@LaunchedEffect
        }

        val target = LatLng(latitude, longitude)

        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                target,
                15.0
            )
        )

        selectedMarker?.let {
            map.removeMarker(it)
        }

        selectedMarker = map.addMarker(
            MarkerOptions()
                .position(target)
                .title("Selected Campsite")
        )
    }
}