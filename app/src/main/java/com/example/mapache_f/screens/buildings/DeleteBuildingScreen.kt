package com.example.mapache_f.screens.buildings

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mapache_f.R
import com.example.mapache_f.screens.map.MyApplication
import com.example.mapache_f.ui.theme.azulTec
import com.example.mapache_f.ui.theme.naranjaTec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteBuildingScreen(navController: NavController) {
    var selectedBuildingName by remember { mutableStateOf("") }
    var buildingNames by remember { mutableStateOf(listOf<String>()) }
    var deletionSuccess by remember { mutableStateOf(false) }
    var isBackButtonEnabled by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val buildingsList = MyApplication.database.buildingDao().getAllBuildings()
                withContext(Dispatchers.Main) {
                    buildingNames = buildingsList.map { it.name }
                    Log.d("DeleteBuildingScreen", "Buildings loaded: $buildingNames")
                }
            }
        }
    }

    fun deleteBuilding(buildingName: String) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val building = MyApplication.database.buildingDao().getBuildingByName(buildingName)
                building?.let {
                    MyApplication.database.buildingDao().deleteBuilding(it)
                    withContext(Dispatchers.Main) {
                        deletionSuccess = true
                        Toast.makeText(context, "Edificio eliminado exitosamente", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                } ?: run {
                    withContext(Dispatchers.Main) {
                        deletionSuccess = false
                        Toast.makeText(context, "Error al eliminar el edificio", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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
                    text = "Eliminar Edificio",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = azulTec,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                )

                var expandedBuildingSpinner by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedBuildingSpinner,
                    onExpandedChange = { expandedBuildingSpinner = !expandedBuildingSpinner }
                ) {
                    TextField(
                        value = selectedBuildingName,
                        onValueChange = { selectedBuildingName = it },
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
                                    selectedBuildingName = buildingName
                                    expandedBuildingSpinner = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (selectedBuildingName.isNotEmpty()) {
                            deleteBuilding(selectedBuildingName)
                        } else {
                            Toast.makeText(context, "Por favor seleccione un edificio para eliminar", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
                ) {
                    Text("Eliminar Edificio", color = Color.White)
                }
            }
        }
    }
}

@Preview
@Composable
fun DeleteBuildingScreenPreview() {
    DeleteBuildingScreen(navController = rememberNavController())
}