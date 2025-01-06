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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterBuildingScreen(navController: NavController) {
    var buildingName by remember { mutableStateOf("") }
    var buildingLatitude by remember { mutableStateOf("") }
    var buildingLongitude by remember { mutableStateOf("") }
    var isBackButtonEnabled by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun registerBuilding() {
        val id = generateUniqueId()
        val name = buildingName.trim()
        val latitude = buildingLatitude.toDoubleOrNull() ?: 0.0
        val longitude = buildingLongitude.toDoubleOrNull() ?: 0.0

        if (name.isNotEmpty()) {
            val building = BuildingEntity(id = id, name = name, lat = latitude, lng = longitude)

            scope.launch {
                withContext(Dispatchers.IO) {
                    MyApplication.database.buildingDao().insertBuildings(listOf(building))
                }
                Toast.makeText(context, "Edificio registrado exitosamente", Toast.LENGTH_SHORT).show()
                navController.navigate("buildingMain") // Navigate back to building buttons screen
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
                    contentDescription = "Atrás",
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
                    text = "Registrar Edificio",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = azulTec,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                )

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
                    onClick = { registerBuilding() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
                ) {
                    Text("Registrar Edificio", color = Color.White)
                }
            }
        }
    }
}

private fun generateUniqueId(): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..12)
        .map { allowedChars.random() }
        .joinToString("")
}

@Preview
@Composable
fun RegisterBuildingScreenPreview() {
    RegisterBuildingScreen(navController = rememberNavController())
}
