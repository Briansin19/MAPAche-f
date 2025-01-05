package com.example.mapache_f.screens.rooms

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
import com.example.mapache_f.ui.theme.naranjaTec
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteRoomScreen(navController: NavController) {
    var selectedRoomName by remember { mutableStateOf("") }
    var roomNames by remember { mutableStateOf(listOf<String>()) }
    var deletionSuccess by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Fetch room names from Firebase
    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
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
                Log.e("DeleteRoomScreen", "Error fetching room names: ${databaseError.message}")
            }
        })
    }

    // Delete room from Firebase
    fun deleteRoom(roomName: String) {
        val database = FirebaseDatabase.getInstance()
        database.getReference("rooms").orderByChild("name").equalTo(roomName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        snapshot.ref.removeValue()
                            .addOnSuccessListener {
                                deletionSuccess = true
                                Toast.makeText(context, "Room deleted successfully", Toast.LENGTH_SHORT).show()
                                navController.popBackStack() // Navigate to the previous screen
                            }
                            .addOnFailureListener { e ->
                                deletionSuccess = false
                                Toast.makeText(context, "Failed to delete room", Toast.LENGTH_SHORT).show()
                                Log.e("DeleteRoomScreen", "Error deleting room: ${e.message}")
                            }
                        return
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    deletionSuccess = false
                    Toast.makeText(context, "Failed to delete room", Toast.LENGTH_SHORT).show()
                    Log.e("DeleteRoomScreen", "Error deleting room: ${databaseError.message}")
                }
            })
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
                    modifier = Modifier.menuAnchor().fillMaxWidth()
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

            // Delete Button
            Button(
                onClick = {
                    if (selectedRoomName.isNotEmpty()) {
                        deleteRoom(selectedRoomName)
                    } else {
                        Toast.makeText(context, "Please select a room to delete", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
            ) {
                Text("Delete Room")
            }
        }
    }
}