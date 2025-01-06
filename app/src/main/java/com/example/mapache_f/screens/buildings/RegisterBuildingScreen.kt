package com.example.mapache_f.screens.buildings

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
fun RegisterBuildingScreen(navController: NavController) {
    var buildingName by remember { mutableStateOf("") }
    var buildingDescription by remember { mutableStateOf("") }
    var buildingLatitude by remember { mutableStateOf("") }
    var buildingLongitude by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun registerBuilding() {
        val name = buildingName.trim()
        val description = buildingDescription.trim()
        val latitude = buildingLatitude.toDoubleOrNull() ?: 0.0
        val longitude = buildingLongitude.toDoubleOrNull() ?: 0.0

        if (name.isNotEmpty() && description.isNotEmpty()) {
            val building = BuildingEntity(name = name, lat = latitude, lng = longitude)

            scope.launch {
                withContext(Dispatchers.IO) {
                    MyApplication.database.buildingDao().insertBuildings(listOf(building))
                }
                Toast.makeText(context, "Building registered successfully", Toast.LENGTH_SHORT).show()
                navController.navigate("buildingMain") // Navigate back to building buttons screen
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
            OutlinedTextField(
                value = buildingName,
                onValueChange = { buildingName = it },
                label = { Text("Nombre del Edificio") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            OutlinedTextField(
                value = buildingDescription,
                onValueChange = { buildingDescription = it },
                label = { Text("Descripción del Edificio") },
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
                onClick = { registerBuilding() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
            ) {
                Text("Registrar Edificio")
            }
        }
    }
}

@Preview
@Composable
fun RegisterBuildingScreenPreview() {
    RegisterBuildingScreen(navController = rememberNavController())
}