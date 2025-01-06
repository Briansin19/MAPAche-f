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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mapache_f.R
import com.example.mapache_f.screens.map.BuildingEntity
import com.example.mapache_f.screens.map.MyApplication
import com.example.mapache_f.ui.theme.azulTec
import com.example.mapache_f.ui.theme.naranjaTec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.font.FontWeight

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
    var isBackButtonEnabled by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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

    LaunchedEffect(selectedBuilding) {
        selectedBuilding?.let {
            buildingId = it.id
            buildingName = it.name
            buildingLatitude = it.lat.toString()
            buildingLongitude = it.lng.toString()
            Log.d("EditBuildingScreen", "Selected building: $it")
        }
    }

    fun updateBuilding() {
        val id = buildingId
        val name = buildingName.trim()
        val latitude = buildingLatitude.toDoubleOrNull() ?: 0.0
        val longitude = buildingLongitude.toDoubleOrNull() ?: 0.0

        if (name.isNotEmpty()) {
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
                    Toast.makeText(context, "Edificio actualizado exitosamente", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }
        } else {
            Toast.makeText(context, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(color = Color.White, modifier = Modifier
        .fillMaxSize()
        .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = {
                    if (isBackButtonEnabled) {
                        isBackButtonEnabled = false
                        navController.popBackStack()
                    }
                },
                enabled = isBackButtonEnabled,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.chevron_left_solid),
                    contentDescription = "AtrÃ¡s",
                    tint = Color.Black
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Editar Edificio",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = azulTec,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                )

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
                        colors = TextFieldDefaults.textFieldColors(
                            focusedIndicatorColor = naranjaTec,
                            unfocusedIndicatorColor = azulTec,
                            cursorColor = naranjaTec
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .padding(8.dp)
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
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = naranjaTec,
                        unfocusedBorderColor = azulTec,
                        cursorColor = naranjaTec
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                OutlinedTextField(
                    value = buildingLatitude,
                    onValueChange = { buildingLatitude = it },
                    label = { Text("Latitud") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = naranjaTec,
                        unfocusedBorderColor = azulTec,
                        cursorColor = naranjaTec
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                OutlinedTextField(
                    value = buildingLongitude,
                    onValueChange = { buildingLongitude = it },
                    label = { Text("Longitud") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = naranjaTec,
                        unfocusedBorderColor = azulTec,
                        cursorColor = naranjaTec
                    ),
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
                    Text("Actualizar Edificio", color = Color.White)
                }
            }
        }
    }
}

@Preview
@Composable
fun EditBuildingScreenPreview() {
    EditBuildingScreen(navController = rememberNavController())
}
