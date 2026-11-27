package com.example.coders.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
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
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import kotlin.math.roundToInt

private const val MAP_STYLE_URL =
    "https://tiles.openfreemap.org/styles/liberty"

/**
 * A campsite reservation to display on the map.
 *
 * @property id Unique reservation or marker ID. It is used to identify the marker when tapped.
 * @property title Text displayed in the marker's info window.
 * @property latitude Campsite latitude.
 * @property longitude Campsite longitude.
 * @property isPast Whether the reservation is a past reservation.
 */
data class CampsiteMapMarker(
    val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val isPast: Boolean = false
)

/** Creates a colored map pin icon for reservation markers. */
private fun createReservationMarkerIcon(
    context: Context,
    color: Int
): Icon {
    val density = context.resources.displayMetrics.density
    val width = (36 * density).roundToInt()
    val height = (48 * density).roundToInt()

    val bitmap = Bitmap.createBitmap(
        width,
        height,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    val centerX = width / 2f
    val path = Path().apply {
        moveTo(centerX, height * 0.98f)
        cubicTo(
            width * 0.70f, height * 0.66f,
            width * 0.90f, height * 0.50f,
            width * 0.90f, height * 0.35f
        )
        cubicTo(
            width * 0.90f, height * 0.15f,
            width * 0.72f, height * 0.03f,
            centerX, height * 0.03f
        )
        cubicTo(
            width * 0.28f, height * 0.03f,
            width * 0.10f, height * 0.15f,
            width * 0.10f, height * 0.35f
        )
        cubicTo(
            width * 0.10f, height * 0.50f,
            width * 0.30f, height * 0.66f,
            centerX, height * 0.98f
        )
        close()
    }

    canvas.drawPath(path, paint)

    paint.color = Color.WHITE
    canvas.drawCircle(
        centerX,
        height * 0.35f,
        width * 0.15f,
        paint
    )

    return IconFactory.getInstance(context).fromBitmap(bitmap)
}

@Composable
fun CampsiteMapPicker(
    modifier: Modifier = Modifier,
    initialLatitude: Double? = null,
    initialLongitude: Double? = null,
    interactive: Boolean = true,
    markers: List<CampsiteMapMarker> = emptyList(),
    onMarkerClick: (CampsiteMapMarker) -> Unit = {},
    // Keep this as the last parameter so existing trailing-lambda calls still work.
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

    var reservationMarkers by remember {
        mutableStateOf<List<Marker>>(emptyList())
    }

    var mapReady by remember {
        mutableStateOf(false)
    }

    val latestCallback by rememberUpdatedState(onLocationSelected)
    val latestInteractive by rememberUpdatedState(interactive)
    val latestMarkers by rememberUpdatedState(markers)
    val latestMarkerCallback by rememberUpdatedState(onMarkerClick)

    val activeMarkerIcon = remember(context) {
        createReservationMarkerIcon(
            context = context,
            color = Color.rgb(46, 125, 50)
        )
    }

    val pastMarkerIcon = remember(context) {
        createReservationMarkerIcon(
            context = context,
            color = Color.rgb(25, 118, 210)
        )
    }

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
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
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
                    onClick = { moveMap(0f, -120f) },
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
                        onClick = { moveMap(-120f, 0f) },
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
                        onClick = { moveMap(120f, 0f) },
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
                    onClick = { moveMap(0f, 120f) },
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

                map.setOnMarkerClickListener { marker ->
                    val selectedReservation = latestMarkers.firstOrNull {
                        it.id == marker.snippet
                    }

                    if (selectedReservation != null) {
                        latestMarkerCallback(selectedReservation)
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

    // Keep the original single selected-location marker behavior for campsite editing.
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
            selectedMarker?.let { map.removeMarker(it) }
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

        selectedMarker?.let { map.removeMarker(it) }
        selectedMarker = map.addMarker(
            MarkerOptions()
                .position(target)
                .title("Selected Campsite")
        )
    }

    // Display all valid reservation markers and adjust the camera to include them.
    LaunchedEffect(mapReady, markers) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (!mapReady) return@LaunchedEffect

        reservationMarkers.forEach { marker ->
            map.removeMarker(marker)
        }
        reservationMarkers = emptyList()

        val validMarkers = markers.filter { marker ->
            marker.latitude.isFinite() &&
                    marker.longitude.isFinite() &&
                    marker.latitude in -90.0..90.0 &&
                    marker.longitude in -180.0..180.0
        }

        reservationMarkers = validMarkers.map { item ->
            val icon = if (item.isPast) pastMarkerIcon else activeMarkerIcon

            map.addMarker(
                MarkerOptions()
                    .position(LatLng(item.latitude, item.longitude))
                    .title(item.title)
                    .snippet(item.id)
                    .icon(icon)
            )
        }

        val uniqueLocations = validMarkers
            .map { it.latitude to it.longitude }
            .distinct()

        if (uniqueLocations.isEmpty()) {
            return@LaunchedEffect
        }

        if (uniqueLocations.size == 1) {
            val first = validMarkers.first()
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(first.latitude, first.longitude),
                    12.0
                )
            )
        } else {
            val boundsBuilder = LatLngBounds.Builder()
            validMarkers.forEach { item ->
                boundsBuilder.include(
                    LatLng(item.latitude, item.longitude)
                )
            }

            val bounds = boundsBuilder.build()
            mapView.post {
                if (mapView.width > 0 && mapView.height > 0) {
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(
                            bounds,
                            80
                        )
                    )
                }
            }
        }
    }
}
