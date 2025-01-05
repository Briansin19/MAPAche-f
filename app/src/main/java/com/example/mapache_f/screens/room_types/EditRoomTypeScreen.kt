package com.example.mapache_f.screens.roomTypes

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mapache_f.classes.RoomTypes // Import your RoomTypes data class
import com.example.mapache_f.ui.theme.naranjaTec
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoomTypeScreen(navController: NavController) {
    var selectedRoomTypeName by remember { mutableStateOf("") }
    var selectedRoomTypeId by remember { mutableStateOf("") }
    var roomTypeName by remember { mutableStateOf("") }
    var roomTypeDescription by remember { mutableStateOf("") }
    var roomTypeNames by remember { mutableStateOf(listOf<String>()) }
    var updateSuccess by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance()

    // Fetch room type names from Firebase
    LaunchedEffect(Unit) {
        database.getReference("room_types").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val names = mutableListOf<String>()
                for (snapshot in dataSnapshot.children) {
                    val roomTypeName = snapshot.child("name").getValue(String::class.java)
                    roomTypeName?.let { names.add(it) }
                }
                roomTypeNames = names
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("EditRoomTypeScreen", "Error fetching room type names: ${databaseError.message}")
            }
        })
    }

    // Fetch selected room type data
    LaunchedEffect(selectedRoomTypeName) {
        if (selectedRoomTypeName.isNotEmpty()) {
            database.getReference("room_types").orderByChild("name").equalTo(selectedRoomTypeName)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val roomType = snapshot.getValue(RoomTypes::class.java)
                            roomType?.let {
                                selectedRoomTypeId = it.id
                                roomTypeName = it.name
                                roomTypeDescription = it.description
                            }
                            return
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("EditRoomTypeScreen", "Error getting selected room type: ${databaseError.message}")
                    }
                })
        }
    }

    // Update room type in Firebase
    fun updateRoomType() {
        if (roomTypeName.isNotEmpty() && roomTypeDescription.isNotEmpty()) {
            val updatedRoomType = RoomTypes(
                id = selectedRoomTypeId,
                name = roomTypeName,
                description = roomTypeDescription
            )

            database.getReference("room_types").child(selectedRoomTypeId).setValue(updatedRoomType)
                .addOnSuccessListener {
                    updateSuccess = true
                    Toast.makeText(context, "Room type updated successfully", Toast.LENGTH_SHORT).show()
                    navController.navigate("roomTypeMain") // Navigate back to room type buttons screen
                }
                .addOnFailureListener { e ->
                    updateSuccess = false
                    Toast.makeText(context, "Failed to update room type", Toast.LENGTH_SHORT).show()
                    Log.e("EditRoomTypeScreen", "Error updating room type: ${e.message}")
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
            // Room Type Spinner
            var expandedRoomTypeSpinner by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedRoomTypeSpinner,
                onExpandedChange = { expandedRoomTypeSpinner = !expandedRoomTypeSpinner }
            ) {
                TextField(
                    value = selectedRoomTypeName,
                    onValueChange = { selectedRoomTypeName = it },
                    readOnly = true,
                    label = { Text("Select Room Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoomTypeSpinner) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedRoomTypeSpinner,
                    onDismissRequest = { expandedRoomTypeSpinner = false }
                ) {
                    roomTypeNames.forEach { roomTypeName ->
                        DropdownMenuItem(
                            text = { Text(roomTypeName) },
                            onClick = {
                                selectedRoomTypeName = roomTypeName
                                expandedRoomTypeSpinner = false
                            }
                        )
                    }
                }
            }

            // Room Type Name
            OutlinedTextField(
                value = roomTypeName,
                onValueChange = { roomTypeName = it },
                label = { Text("Room Type Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            // Room Type Description
            OutlinedTextField(
                value = roomTypeDescription,
                onValueChange = { roomTypeDescription = it },
                label = { Text("Room Type Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            // Update Button
            Button(
                onClick = { updateRoomType() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
            ) {
                Text("Update Room Type")
            }
        }
    }
}