package com.example.mapache_f.screens.map

import android.Manifest
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

import androidx.lifecycle.ViewModel
import com.example.mapache_f.ui.theme.MAPAchefTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewModelScope
import com.example.mapache_f.screens.map.BuildingEntity
import com.example.mapache_f.screens.map.MyApplication
import com.example.mapache_f.screens.map.OpenRouteServiceClient
import com.example.mapache_f.screens.map.RetrofitClient
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
//import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import retrofit2.await


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment


class MapFragment : Fragment() {

    private val viewModel: MapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(requireContext(), requireContext().getSharedPreferences("osmdroid", MODE_PRIVATE))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MAPAchefTheme() {
                    MapScreen(viewModel)
                }
            }
        }
    }
}

class MapViewModel : ViewModel() {
    var mapView: MapView? = null
    private var locationOverlay: MyLocationNewOverlay? = null

    var userLocation: GeoPoint? = null
    var bounds: List<GeoPoint>? = null

    // Función para obtener datos desde la API y actualizar el mapa
    fun fetchMapData() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getMapData().await()
                }

                Log.d("Retrofit", "Datos obtenidos: $response")

                // Procesar la respuesta
                userLocation = GeoPoint(response.user_location.lat, response.user_location.lng)
                bounds = response.bounds.map { GeoPoint(it.lat, it.lng) }

                withContext(Dispatchers.Main) {
                    mapView?.let { mv ->
                        if (bounds != null && bounds!!.size == 2) {
                            val southWest = bounds!![1]
                            val northEast = bounds!![0]

                            // Calcular el bounding box asegurando la orden correcta
                            val latNorth = maxOf(northEast.latitude, southWest.latitude)
                            val latSouth = minOf(northEast.latitude, southWest.latitude)
                            val lonEast = maxOf(northEast.longitude, southWest.longitude)
                            val lonWest = minOf(northEast.longitude, southWest.longitude)

                            val boundingBox = org.osmdroid.util.BoundingBox(latNorth, lonEast, latSouth, lonWest)

                            // Opcional: si quieres restringir el scroll al bounding box
                            mv.setScrollableAreaLimitDouble(boundingBox)

                            // Luego haces el zoom
                            mv.zoomToBoundingBox(boundingBox, true, 50)

                        }

                        userLocation?.let { loc ->
                            val marker = Marker(mv)
                            marker.position = loc
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            marker.title = "Ubicación inicial del usuario"
                            mv.overlays.add(marker)
                            mv.invalidate()
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Retrofit", "Error al obtener datos: ${e.message}")
            }
        }
    }

    // Función para cargar los edificios desde la API y mostrarlos en el mapa
    fun loadBuildings() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.buildingApi.getBuildings().execute()
                }
                if (response.isSuccessful && response.body() != null) {
                    val buildings = response.body()!!
                    Log.d("Building", "Cantidad de edificios obtenidos: ${buildings.size}")
                    // Convertir a BuildingEntity
                    val entities = buildings.map { b ->
                        BuildingEntity(name = b.name, lat = b.lat, lng = b.lng)
                    }

                    withContext(Dispatchers.IO) {
                        MyApplication.database.buildingDao().clear()
                        MyApplication.database.buildingDao().insertBuildings(entities)
                        Log.d("Building", "Cantidad de edificios insertados: ${entities.size}")
                    }

                    // Ahora ya están en la BD local
                    // Podemos leerlos y mostrarlos en el mapa
                    val localBuildings = withContext(Dispatchers.IO) {
                        MyApplication.database.buildingDao().getAllBuildings()
                    }
                    Log.d("Building", "Cantidad de edificios leídos de la BD local: ${localBuildings.size}")

                    withContext(Dispatchers.Main) {
                        mapView?.let { mv ->
                            // Añadir marcadores
                            for (b in localBuildings) {
                                val marker = Marker(mv)
                                marker.position = GeoPoint(b.lat, b.lng)
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                marker.title = b.name
                                mv.overlays.add(marker)
                            }
                            mv.invalidate()
                        }
                    }

                } else {
                    Log.e("Building", "Error al cargar edificios: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Building", "Error: ${e.message}")
            }
        }
    }

    // Función para habilitar la ubicación del usuario
    fun enableMyLocation() {
        mapView?.let {
            locationOverlay = MyLocationNewOverlay(it).apply {
                enableMyLocation()
                enableFollowLocation()
                runOnFirstFix {
                    val myLocation = myLocation
                    if (myLocation != null) {
                        it.controller.setCenter(GeoPoint(myLocation.latitude, myLocation.longitude))
                    }
                }
            }
            it.overlays.add(locationOverlay)
        }
    }

    // Función para centrar el mapa en la ubicación del usuario
    fun centerOnMyLocation() {
        locationOverlay?.myLocation?.let {
            mapView?.controller?.setCenter(GeoPoint(it.latitude, it.longitude))
            mapView?.controller?.setZoom(18.0)
        }
    }

    // Función para obtener la ruta desde OpenRouteService (perfil wheelchair)
    fun fetchRoute(origin: GeoPoint, destination: GeoPoint) {
        viewModelScope.launch {
            try {
                val orsApiKey = "5b3ce3597851110001cf6248783e1be43412426192c979cbbb8abc19"
                val start = "${origin.longitude},${origin.latitude}"
                val end = "${destination.longitude},${destination.latitude}"

                var routeOverlay: Polyline? = null

                val response = withContext(Dispatchers.IO) {
                    OpenRouteServiceClient.api.getDirections(orsApiKey, start, end).execute()
                }

                if (response.isSuccessful && response.body() != null) {
                    val orsResponse = response.body()!!
                    if (orsResponse.features.isNotEmpty()) {
                        val feature = orsResponse.features[0]
                        val routeCoords = feature.geometry.coordinates.map { coord ->
                            GeoPoint(coord[1], coord[0]) // (lat, lon)
                        }

                        withContext(Dispatchers.Main) {
                            mapView?.let { mv ->
                                // Si ya existe una ruta anterior, la removemos
                                routeOverlay?.let { mv.overlays.remove(it) }

                                val line = Polyline()
                                line.setPoints(routeCoords)
                                line.color = android.graphics.Color.BLUE
                                line.width = 5f
                                mv.overlays.add(line)
                                routeOverlay = line  // Guardamos la nueva polyline
                                mv.invalidate()

                                val boundingBox = org.osmdroid.util.BoundingBox.fromGeoPoints(routeCoords)
                                mv.zoomToBoundingBox(boundingBox, true, 50)
                            }
                        }
                    } else {
                        Log.e("ORS", "No route found")
                    }
                } else {
                    Log.e("ORS", "Failed to fetch route: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ORS", "Error: ${e.message}")
            }
        }
    }

    suspend fun findBuildingByName(name: String): BuildingEntity? {
        return withContext(Dispatchers.IO) {
            val buildings = MyApplication.database.buildingDao().getAllBuildings()
            Log.d("Search", "Buscando edificio con nombre: $name")
            Log.d("Search", "Edificios en BD: ${buildings.map { it.name }}")
            buildings.find { it.name.contains(name, ignoreCase = true) }
        }
    }

    override fun onCleared() {
        locationOverlay?.disableMyLocation()
        locationOverlay = null
        mapView = null
        super.onCleared()
    }
}

@Composable
fun MapScreen(viewModel: MapViewModel) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var gesturesEnabled by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(false) }
    var isAuthenticated by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }
    var userName by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser?.displayName?.split(" ")?.firstOrNull() ?: "") }
    var isAdmin by remember { mutableStateOf(false) }

    val adminEmails = listOf("l20390288@chetumal.tecnm.mx", "l20390301@chetumal.tecnm.mx")

    LaunchedEffect(drawerState.isOpen) {
        gesturesEnabled = drawerState.isOpen
    }

    LaunchedEffect(Unit) {
        viewModel.enableMyLocation()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Option 1", modifier = Modifier.padding(16.dp))
                        Text("Option 2", modifier = Modifier.padding(16.dp))
                        Text("Option 3", modifier = Modifier.padding(16.dp))
                        if (isAdmin) {
                            Text("Configuration", modifier = Modifier.padding(16.dp))
                        }
                    }
                    Box(contentAlignment = Alignment.CenterEnd) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = {
                                    if (isAuthenticated) {
                                        FirebaseAuth.getInstance().signOut()
                                        isAuthenticated = false
                                        userName = ""
                                        isAdmin = false
                                        val sharedPreferences = context.getSharedPreferences("auth", MODE_PRIVATE)
                                        sharedPreferences.edit().clear().apply()
                                        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                                        Log.d("Auth", "User logged out")
                                    } else {
                                        isLoading = true
                                        val provider = OAuthProvider.newBuilder("microsoft.com")
                                        provider.addCustomParameter("prompt", "login")
                                        provider.addCustomParameter("tenant", "9af622ec-f1a5-4422-bdc9-8c20039ed9eb")

                                        FirebaseAuth.getInstance()
                                            .startActivityForSignInWithProvider(
                                                context as ComponentActivity,
                                                provider.build()
                                            )
                                            .addOnSuccessListener { authResult ->
                                                isLoading = false
                                                isAuthenticated = true
                                                val user = authResult.user
                                                userName = user?.displayName?.split(" ")?.firstOrNull() ?: "User"
                                                isAdmin = user?.email in adminEmails
                                                Log.d("Auth", "User logged in: ${user?.email}, isAdmin: $isAdmin")
                                            }
                                            .addOnFailureListener {
                                                isLoading = false
                                                Toast.makeText(
                                                    context,
                                                    "Error de inicio de sesión: ${it.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                Log.d("Auth", "Login failed: ${it.message}")
                                            }
                                    }
                                },
                                modifier = Modifier.padding(8.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                } else {
                                    Text(if (isAuthenticated) "Logout" else "Login")
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            if (isAuthenticated) {
                                Text("Hi, $userName", modifier = Modifier.padding(end = 8.dp))
                            }
                        }
                    }
                }
            }
        },
        content = {
            Box(modifier = Modifier.fillMaxSize()) {

                val edificioW = GeoPoint(18.519017, -88.302848)

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        MapView(context).apply {
                            Configuration.getInstance().setMapViewHardwareAccelerated(false)

                            // Deshabilitar las repeticiones
                            isHorizontalMapRepetitionEnabled = false
                            isVerticalMapRepetitionEnabled = false

                            setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            viewModel.mapView = this
                            viewModel.fetchMapData()
                            viewModel.loadBuildings()
                        }
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 32.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search here...", color = Color.White) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White
                        )
                    )
                    IconButton(onClick = {
                        scope.launch {
                            try {
                                val entrada = GeoPoint(18.519261, -88.302347) // Punto de entrada
                                val buildingName = searchQuery.text
                                val buildingFound = viewModel.findBuildingByName(buildingName)
                                if (buildingFound != null) {
                                    viewModel.fetchRoute(entrada, GeoPoint(buildingFound.lat, buildingFound.lng))
                                } else {
                                    Toast.makeText(context, "Edificio no encontrado", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Log.e("MapScreen", "Error al buscar edificio: ${e.message}")
                                Toast.makeText(context, "Error al buscar edificio: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                }

                FloatingActionButton(
                    onClick = { viewModel.centerOnMyLocation() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Show My Location")
                }

                FloatingActionButton(
                    onClick = { scope.launch { drawerState.open() } },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }

            }
        }
    )
}