package com.example.mapache_f.screens.rooms

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mapache_f.classes.Room // Import your Room data class
import com.example.mapache_f.ui.theme.naranjaTec
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoomScreen(navController: NavController) {
    var selectedRoomName by remember { mutableStateOf("") }
    var selectedRoomId by remember { mutableStateOf("") }
    var roomName by remember { mutableStateOf("") }
    var roomDescription by remember { mutableStateOf("") }
    var selectedRoomTypeId by remember { mutableStateOf("") }
    var selectedBuildingId by remember { mutableStateOf("") }
    var floorNumber by remember { mutableStateOf("") }
    var roomNames by remember { mutableStateOf(listOf<String>()) }
    var roomTypeNames by remember { mutableStateOf(listOf<String>()) }
    var roomTypeIdMap by remember { mutableStateOf(mapOf<String, String>()) }
    var buildingNames by remember { mutableStateOf(listOf<String>()) }
    var buildingIdMap by remember { mutableStateOf(mapOf<String, String>()) }
    var updateSuccess by remember { mutableStateOf(false) }

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
                Log.w("EditRoomScreen", "Error getting room types", databaseError.toException())
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
                Log.w("EditRoomScreen", "Error getting buildings", databaseError.toException())
            }
        })
    }

    // Fetch room names from Firebase
    LaunchedEffect(Unit) {
        database.getReference("rooms").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val names = mutableListOf<String>()
                for (snapshot in dataSnapshot.children) {
                    val roomName = snapshot.child("name").getValue(String::class.java)
                    roomName?.let { names.add(it) }
                }
                roomNames = names
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("EditRoomScreen", "Error fetching room names: ${databaseError.message}")
            }
        })
    }

    // Fetch selected room data
    LaunchedEffect(selectedRoomName) {
        if (selectedRoomName.isNotEmpty()) {
            database.getReference("rooms").orderByChild("name").equalTo(selectedRoomName)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val room = snapshot.getValue(Room::class.java)
                            room?.let {
                                selectedRoomId = it.id
                                roomName = it.name
                                roomDescription = it.description
                                selectedRoomTypeId = it.roomTypeId
                                selectedBuildingId = it.buildingId
                                floorNumber = it.floorNumber.toString()

                                // Set room type and building spinners
                                val roomTypeName = roomTypeIdMap.entries.find { it.value == selectedRoomTypeId }?.key
                                roomTypeName?.let { selectedRoomTypeId = it }

                                val buildingName = buildingIdMap.entries.find { it.value == selectedBuildingId }?.key
                                buildingName?.let { selectedBuildingId = it }
                            }
                            return
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("EditRoomScreen", "Error getting selected room: ${databaseError.message}")
                    }
                })
        }
    }

    // Update room in Firebase
    fun updateRoom() {
        val roomTypeId = roomTypeIdMap[selectedRoomTypeId]
        val buildingId = buildingIdMap[selectedBuildingId]
        val floor = floorNumber.toIntOrNull() ?: 0

        if (roomName.isNotEmpty() && roomDescription.isNotEmpty() && roomTypeId != null && buildingId != null) {
            val updatedRoom = Room(
                id = selectedRoomId,
                name = roomName,
                description = roomDescription,
                roomTypeId = roomTypeId,
                buildingId = buildingId,
                floorNumber = floor
            )

            database.getReference("rooms").child(selectedRoomId).setValue(updatedRoom)
                .addOnSuccessListener {
                    updateSuccess = true
                    Toast.makeText(context, "Room updated successfully", Toast.LENGTH_SHORT).show()
                    navController.navigate("roomMain")
                }
                .addOnFailureListener { e ->
                    updateSuccess = false
                    Toast.makeText(context, "Failed to update room", Toast.LENGTH_SHORT).show()
                    Log.e("EditRoomScreen", "Error updating room: ${e.message}")
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
            // Room Spinner
            var expandedRoomSpinner by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedRoomSpinner,
                onExpandedChange = { expandedRoomSpinner = !expandedRoomSpinner }
            ) {
                TextField(
                    value = selectedRoomName,
                    onValueChange = { selectedRoomName = it },
                    readOnly = true,
                    label = { Text("Select Room") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoomSpinner) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedRoomSpinner,
                    onDismissRequest = { expandedRoomSpinner = false }
                ) {
                    roomNames.forEach { roomName ->
                        DropdownMenuItem(
                            text = { Text(roomName) },
                            onClick = {
                                selectedRoomName = roomName
                                expandedRoomSpinner = false
                            }
                        )
                    }
                }
            }

            // Room Name
            OutlinedTextField(
                value = roomName,
                onValueChange = { roomName = it },
                label = { Text("Room Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            // Room Description
            OutlinedTextField(
                value = roomDescription,
                onValueChange = { roomDescription = it },
                label = { Text("Room Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            // Room Type Spinner
            var expandedRoomTypeSpinner by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedRoomTypeSpinner,
                onExpandedChange = { expandedRoomTypeSpinner = !expandedRoomTypeSpinner }
            ) {
                TextField(
                    value = selectedRoomTypeId,
                    onValueChange = { selectedRoomTypeId = it },
                    readOnly = true,
                    label = { Text("Room Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoomTypeSpinner) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = expandedRoomTypeSpinner,
                    onDismissRequest = { expandedRoomTypeSpinner = false }
                ) {
                    roomTypeNames.forEach { roomTypeName -> // Assuming you have a roomTypeNames state variable
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
            var expandedBuildingSpinner by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedBuildingSpinner,
                onExpandedChange = { expandedBuildingSpinner = !expandedBuildingSpinner }
            ) {
                TextField(
                    value = selectedBuildingId,
                    onValueChange = { selectedBuildingId = it },
                    readOnly = true,
                    label = { Text("Building") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBuildingSpinner) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = expandedBuildingSpinner,
                    onDismissRequest = { expandedBuildingSpinner = false }
                ) {
                    buildingNames.forEach { buildingName -> // Assuming you have a buildingNames state variable
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

            // Floor Number
            OutlinedTextField(
                value = floorNumber,
                onValueChange = { newValue ->
                    floorNumber = newValue.filter { it.isDigit() }
                },
                label = { Text("Floor Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            // Update Button
            Button(
                onClick = { updateRoom() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
            ) {
                Text("Update Room")
            }
        }
    }
}