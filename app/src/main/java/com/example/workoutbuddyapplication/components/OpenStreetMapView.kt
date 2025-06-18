package com.example.workoutbuddyapplication.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import com.example.workoutbuddyapplication.utils.LatLng

// Carto tile sources
val CARTO_POSITRON = XYTileSource(
    "CartoPositron",
    0, 18, 256, ".png",
    arrayOf(
        "https://a.basemaps.cartocdn.com/light_all/",
        "https://b.basemaps.cartocdn.com/light_all/",
        "https://c.basemaps.cartocdn.com/light_all/",
        "https://d.basemaps.cartocdn.com/light_all/"
    ),
    "© CartoDB, © OpenStreetMap contributors"
)

val CARTO_DARK_MATTER = XYTileSource(
    "CartoDarkMatter", 
    0, 18, 256, ".png",
    arrayOf(
        "https://a.basemaps.cartocdn.com/dark_all/",
        "https://b.basemaps.cartocdn.com/dark_all/",
        "https://c.basemaps.cartocdn.com/dark_all/",
        "https://d.basemaps.cartocdn.com/dark_all/"
    ),
    "© CartoDB, © OpenStreetMap contributors"
)

val CARTO_VOYAGER = XYTileSource(
    "CartoVoyager",
    0, 18, 256, ".png", 
    arrayOf(
        "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://c.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://d.basemaps.cartocdn.com/rastertiles/voyager/"
    ),
    "© CartoDB, © OpenStreetMap contributors"
)

enum class CartoMapStyle {
    POSITRON,
    DARK_MATTER,
    VOYAGER
}

fun createLocationDotDrawable(context: Context): Drawable {
    return ShapeDrawable(OvalShape()).apply {
        intrinsicWidth = 48
        intrinsicHeight = 48
        
        paint.apply {
            color = Color.Blue.toArgb()
            isAntiAlias = true
        }
        
        setBounds(0, 0, 48, 48)
    }
}

class RedLocationDotDrawable : Drawable() {
    private val borderPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val centerPaint = Paint().apply {
        color = Color.Red.toArgb()
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        val centerX = bounds.exactCenterX()
        val centerY = bounds.exactCenterY()
        val radius = bounds.width() / 2f

        canvas.drawCircle(centerX, centerY, radius, borderPaint)
        canvas.drawCircle(centerX, centerY, radius * 0.6f, centerPaint)
    }

    override fun setAlpha(alpha: Int) {
        borderPaint.alpha = alpha
        centerPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
        borderPaint.colorFilter = colorFilter
        centerPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT

    override fun getIntrinsicWidth(): Int = 60
    override fun getIntrinsicHeight(): Int = 60
}



class LocationDotDrawable : Drawable() {
    private val borderPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val centerPaint = Paint().apply {
        color = Color.Blue.toArgb()
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    override fun draw(canvas: Canvas) {
        val bounds = bounds
        val centerX = bounds.exactCenterX()
        val centerY = bounds.exactCenterY()
        val radius = bounds.width() / 2f
        
        canvas.drawCircle(centerX, centerY, radius, borderPaint)
        
        canvas.drawCircle(centerX, centerY, radius * 0.6f, centerPaint)
    }
    
    override fun setAlpha(alpha: Int) {
        borderPaint.alpha = alpha
        centerPaint.alpha = alpha
    }
    
    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
        borderPaint.colorFilter = colorFilter
        centerPaint.colorFilter = colorFilter
    }
    
    override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
}

class StartMarkerDrawable : Drawable() {
    private val borderPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val centerPaint = Paint().apply {
        color = Color.Green.toArgb()
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    override fun draw(canvas: Canvas) {
        val bounds = bounds
        val centerX = bounds.exactCenterX()
        val centerY = bounds.exactCenterY()
        val radius = bounds.width() / 2f
        
        canvas.drawCircle(centerX, centerY, radius, borderPaint)
        
        canvas.drawCircle(centerX, centerY, radius * 0.7f, centerPaint)
    }
    
    override fun setAlpha(alpha: Int) {
        borderPaint.alpha = alpha
        centerPaint.alpha = alpha
    }
    
    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
        borderPaint.colorFilter = colorFilter
        centerPaint.colorFilter = colorFilter
    }
    
    override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
}

@Composable
fun OpenStreetMapView(
    modifier: Modifier = Modifier,
    currentLocation: LatLng?,
    routePoints: List<LatLng>,
    mapStyle: CartoMapStyle = CartoMapStyle.POSITRON,
    onMapReady: (MapView) -> Unit = {}
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    
    // Configure osmdroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = "WorkoutBuddyApp"
    }
    
    // Update route when points change
    LaunchedEffect(routePoints) {
        mapView?.let { map ->
            // Clear existing route overlays but keep markers
            val overlaysToKeep = map.overlays.filter { overlay ->
                overlay is MyLocationNewOverlay || 
                (overlay is Marker && (overlay.title == "Huidige locatie" || overlay.title == "Start"))
            }
            map.overlays.clear()
            map.overlays.addAll(overlaysToKeep)
            
            // Add route polyline if we have points
            if (routePoints.size > 1) {
                val polyline = Polyline(map).apply {
                    setPoints(routePoints.map { GeoPoint(it.latitude, it.longitude) })
                    outlinePaint.color = Color.Blue.toArgb()
                    outlinePaint.strokeWidth = 8f
                }
                map.overlays.add(polyline)
            }
            
            // Add start marker if we don't already have one and have route points
            if (routePoints.isNotEmpty() && !map.overlays.any { it is Marker && it.title == "Start" }) {
                val startMarker = Marker(map).apply {
                    position = GeoPoint(routePoints.first().latitude, routePoints.first().longitude)
                    title = "Start"
                    snippet = "Startpunt van je run"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    
                    // Use custom green dot drawable for start
                    icon = StartMarkerDrawable().apply {
                        setBounds(0, 0, 60, 60)
                    }
                }
                map.overlays.add(startMarker)
            }
            
            map.invalidate()
        }
    }
    
    // Update current location marker separately
    LaunchedEffect(currentLocation) {
        mapView?.let { map ->
            // Remove old current location marker
            map.overlays.removeAll { it is Marker && it.title == "Huidige locatie" }
            
            // Add current location marker if available
            currentLocation?.let { location ->
                val currentMarker = Marker(map).apply {
                    position = GeoPoint(location.latitude, location.longitude)
                    title = "Huidige locatie"
                    snippet = "Je bent hier"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    
                    // Use custom blue dot drawable
                    icon = RedLocationDotDrawable().apply {
                        setBounds(0, 0, 60, 60)
                    }
                }
                map.overlays.add(currentMarker)
                
                // Center map on current location
                map.controller.setCenter(GeoPoint(location.latitude, location.longitude))
            }
            
            map.invalidate()
        }
    }
    
    if (currentLocation == null) {
        // Show loading state while waiting for GPS
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("GPS-signaal zoeken...")
            }
        }
    } else {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                MapView(context).apply {
                    setTileSource(when (mapStyle) {
                        CartoMapStyle.POSITRON -> CARTO_POSITRON
                        CartoMapStyle.DARK_MATTER -> CARTO_DARK_MATTER
                        CartoMapStyle.VOYAGER -> CARTO_VOYAGER
                    })
                    setMultiTouchControls(true)
                    controller.setZoom(16.0)
                    
                    // Set initial location
                    currentLocation?.let { location ->
                        controller.setCenter(GeoPoint(location.latitude, location.longitude))
                    }
                    
                    mapView = this
                    onMapReady(this)
                }
            },
            update = { map ->
                mapView = map
            }
        )
    }
} 