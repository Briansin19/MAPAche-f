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
fun DeleteBuildingScreen(navController: NavController) {
    var selectedBuildingName by remember { mutableStateOf("") }
    var buildingNames by remember { mutableStateOf(listOf<String>()) }
    var deletionSuccess by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Fetch building names from Room
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

    // Delete building from Room
    fun deleteBuilding(buildingName: String) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val building = MyApplication.database.buildingDao().getBuildingByName(buildingName)
                building?.let {
                    MyApplication.database.buildingDao().deleteBuilding(it)
                    withContext(Dispatchers.Main) {
                        deletionSuccess = true
                        Toast.makeText(context, "Building deleted successfully", Toast.LENGTH_SHORT).show()
                        navController.popBackStack() // Navigate to the previous screen
                    }
                } ?: run {
                    withContext(Dispatchers.Main) {
                        deletionSuccess = false
                        Toast.makeText(context, "Failed to delete building", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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
                                selectedBuildingName = buildingName
                                expandedBuildingSpinner = false
                            }
                        )
                    }
                }
            }

            // Delete Button
            Button(
                onClick = {
                    if (selectedBuildingName.isNotEmpty()) {
                        deleteBuilding(selectedBuildingName)
                    } else {
                        Toast.makeText(context, "Please select a building to delete", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
            ) {
                Text("Delete Building")
            }
        }
    }
}

@Preview
@Composable
fun DeleteBuildingScreenPreview() {
    DeleteBuildingScreen(navController = rememberNavController())
}