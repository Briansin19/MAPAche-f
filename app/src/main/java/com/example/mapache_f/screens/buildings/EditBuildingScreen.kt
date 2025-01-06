package com.example.mapache_f.screens.buildings

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mapache_f.screens.map.BuildingEntity
import com.example.mapache_f.screens.map.MyApplication
import com.example.mapache_f.ui.theme.naranjaTec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBuildingScreen(navController: NavController) {
    var buildingId by remember { mutableStateOf("") }
    var buildingName by remember { mutableStateOf("") }
    var buildingLatitude by remember { mutableStateOf("") }
    var buildingLongitude by remember { mutableStateOf("") }
    var buildingNames by remember { mutableStateOf(listOf<String>()) }
    var selectedBuilding by remember { mutableStateOf<BuildingEntity?>(null) }
    var expandedBuildingSpinner by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Fetch building names from Room
    LaunchedEffect(Unit) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val buildingsList = MyApplication.database.buildingDao().getAllBuildings()
                withContext(Dispatchers.Main) {
                    buildingNames = buildingsList.map { it.name }
                    Log.d("EditBuildingScreen", "Buildings loaded: $buildingNames")
                }
            }
        }
    }

    // Load selected building details
    LaunchedEffect(selectedBuilding) {
        selectedBuilding?.let {
            buildingId = it.id
            buildingName = it.name
            buildingLatitude = it.lat.toString()
            buildingLongitude = it.lng.toString()
            Log.d("EditBuildingScreen", "Selected building: $it")
        }
    }

    // Update building in Room
    fun updateBuilding() {
        val id = buildingId
        val name = buildingName.trim()
        val latitude = buildingLatitude.toDoubleOrNull() ?: 0.0
        val longitude = buildingLongitude.toDoubleOrNull() ?: 0.0

        if (name.isNotEmpty()) {
            // Create a new BuildingEntity object with updated values
            val updatedBuilding = BuildingEntity(
                id = id,
                name = name,
                lat = latitude,
                lng = longitude
            )

            updatedBuilding?.let {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        MyApplication.database.buildingDao().updateBuilding(it)
                    }
                    Toast.makeText(context, "Building updated successfully", Toast.LENGTH_SHORT).show()
                    navController.popBackStack() // Navigate back
                }
            }
        } else {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Building Spinner
            ExposedDropdownMenuBox(
                expanded = expandedBuildingSpinner,
                onExpandedChange = { expandedBuildingSpinner = !expandedBuildingSpinner }
            ) {
                TextField(
                    value = selectedBuilding?.name ?: "Selecciona un edificio",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Edificio") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBuildingSpinner) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedBuildingSpinner,
                    onDismissRequest = { expandedBuildingSpinner = false }
                ) {
                    buildingNames.forEach { buildingName ->
                        DropdownMenuItem(
                            text = { Text(buildingName) },
                            onClick = {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        val building = MyApplication.database.buildingDao().getBuildingByName(buildingName)
                                        withContext(Dispatchers.Main) {
                                            selectedBuilding = building
                                            expandedBuildingSpinner = false
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = buildingName,
                onValueChange = { buildingName = it },
                label = { Text("Nombre del Edificio") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            OutlinedTextField(
                value = buildingLatitude,
                onValueChange = { buildingLatitude = it },
                label = { Text("Latitud") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            OutlinedTextField(
                value = buildingLongitude,
                onValueChange = { buildingLongitude = it },
                label = { Text("Longitud") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            Button(
                onClick = { updateBuilding() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
            ) {
                Text("Actualizar Edificio")
            }
        }
    }
}

@Preview
@Composable
fun EditBuildingScreenPreview() {
    EditBuildingScreen(navController = rememberNavController())
}