package com.example.mapache_f.screens.rooms

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mapache_f.R
import com.example.mapache_f.classes.Room
import com.example.mapache_f.ui.theme.azulTec
import com.example.mapache_f.ui.theme.naranjaTec
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterRoomScreen(navController: NavController) {
    var roomName by remember { mutableStateOf("") }
    var roomDescription by remember { mutableStateOf("") }
    var selectedRoomTypeId by remember { mutableStateOf("") }
    var selectedBuildingId by remember { mutableStateOf("") }
    var floorNumber by remember { mutableStateOf("") }

    var roomTypeNames by remember { mutableStateOf(listOf<String>()) }
    var roomTypeIdMap by remember { mutableStateOf(mapOf<String, String>()) }
    var buildingNames by remember { mutableStateOf(listOf<String>()) }
    var buildingIdMap by remember { mutableStateOf(mapOf<String, String>()) }

    var expandedRoomTypeSpinner by remember { mutableStateOf(false) }
    var expandedBuildingSpinner by remember { mutableStateOf(false) }
    var isBackButtonEnabled by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance()

    // Fetch room types from Firebase
    LaunchedEffect(Unit) {
        database.getReference("room_types").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val names = mutableListOf<String>()
                val idMap = mutableMapOf<String, String>()
                for (snapshot in dataSnapshot.children) {
                    val roomTypeName = snapshot.child("name").getValue(String::class.java)
                    val roomTypeId = snapshot.key
                    if (roomTypeName != null && roomTypeId != null) {
                        names.add(roomTypeName)
                        idMap[roomTypeName] = roomTypeId
                    }
                }
                roomTypeNames = names
                roomTypeIdMap = idMap
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("RegisterRoomScreen", "Error getting room types", databaseError.toException())
            }
        })
    }

    // Fetch buildings from Firebase
    LaunchedEffect(Unit) {
        database.getReference("buildings").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val names = mutableListOf<String>()
                val idMap = mutableMapOf<String, String>()
                for (snapshot in dataSnapshot.children) {
                    val buildingName = snapshot.child("name").getValue(String::class.java)
                    val buildingId = snapshot.key
                    if (buildingName != null && buildingId != null) {
                        names.add(buildingName)
                        idMap[buildingName] = buildingId
                    }
                }
                buildingNames = names
                buildingIdMap = idMap
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("RegisterRoomScreen", "Error getting buildings", databaseError.toException())
            }
        })
    }

    fun registerRoom() {
        val name = roomName.trim()
        val description = roomDescription.trim()
        val roomTypeId = roomTypeIdMap[selectedRoomTypeId]
        val buildingId = buildingIdMap[selectedBuildingId]
        val floor = floorNumber.toIntOrNull() ?: 0

        if (name.isNotEmpty() && description.isNotEmpty() && roomTypeId != null && buildingId != null) {
            val id = generateUniqueId()
            val room = Room(id, name, description, roomTypeId, buildingId, floor)

            val database = FirebaseDatabase.getInstance()
            val roomsRef = database.getReference("rooms")

            roomsRef.child(id).setValue(room)
                .addOnSuccessListener {
                    Log.d("RegisterRoom", "Room stored successfully")
                    Toast.makeText(context, "Lugar registrada exitosamente", Toast.LENGTH_SHORT).show()
                    navController.navigate("roomMain") // Navigate back to room buttons screen
                }
                .addOnFailureListener { e ->
                    Log.w("RegisterRoom", "Error storing Room", e)
                    Toast.makeText(context, "Error al registrar la lugar", Toast.LENGTH_SHORT).show()
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
                    text = "Registrar Lugar",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = azulTec,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = { Text("Nombre del Lugar") },
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
                    value = roomDescription,
                    onValueChange = { roomDescription = it },
                    label = { Text("Descripción del Lugar") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = naranjaTec,
                        unfocusedBorderColor = azulTec,
                        cursorColor = naranjaTec
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                // Room Type Spinner
                ExposedDropdownMenuBox(
                    expanded = expandedRoomTypeSpinner,
                    onExpandedChange = { expandedRoomTypeSpinner = !expandedRoomTypeSpinner }
                ) {
                    TextField(
                        value = selectedRoomTypeId,
                        onValueChange = { selectedRoomTypeId = it },
                        readOnly = true,
                        label = { Text("Tipo de Lugar") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoomTypeSpinner) },
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
                        expanded = expandedRoomTypeSpinner,
                        onDismissRequest = { expandedRoomTypeSpinner = false }
                    ) {
                        roomTypeNames.forEach { roomTypeName ->
                            DropdownMenuItem(
                                text = { Text(roomTypeName) },
                                onClick = {
                                    selectedRoomTypeId = roomTypeName
                                    expandedRoomTypeSpinner = false
                                }
                            )
                        }
                    }
                }

                // Building Spinner
                ExposedDropdownMenuBox(
                    expanded = expandedBuildingSpinner,
                    onExpandedChange = { expandedBuildingSpinner = !expandedBuildingSpinner }
                ) {
                    TextField(
                        value = selectedBuildingId,
                        onValueChange = { selectedBuildingId = it },
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
                                    selectedBuildingId = buildingName
                                    expandedBuildingSpinner = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = floorNumber,
                    onValueChange = { newValue ->
                        floorNumber = newValue.filter { it.isDigit() }
                    },
                    label = { Text("Número de Piso") },
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
                    onClick = { registerRoom() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
                ) {
                    Text("Registrar Lugar", color = Color.White)
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
