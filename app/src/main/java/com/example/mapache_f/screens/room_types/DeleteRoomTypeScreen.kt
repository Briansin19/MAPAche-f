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
import com.example.mapache_f.ui.theme.naranjaTec
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteRoomTypeScreen(navController: NavController) {
    var selectedRoomTypeName by remember { mutableStateOf("") }
    var roomTypeNames by remember { mutableStateOf(listOf<String>()) }
    var deletionSuccess by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Fetch room type names from Firebase
    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
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
                Log.e("DeleteRoomTypeScreen", "Error fetching room type names: ${databaseError.message}")
            }
        })
    }

    // Delete room type from Firebase
    fun deleteRoomType(roomTypeName: String) {
        val database = FirebaseDatabase.getInstance()
        database.getReference("room_types").orderByChild("name").equalTo(roomTypeName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        snapshot.ref.removeValue()
                            .addOnSuccessListener {
                                deletionSuccess = true
                                Toast.makeText(context, "Room type deleted successfully", Toast.LENGTH_SHORT).show()
                                navController.popBackStack() // Navigate to the previous screen
                            }
                            .addOnFailureListener { e ->
                                deletionSuccess = false
                                Toast.makeText(context, "Failed to delete room type", Toast.LENGTH_SHORT).show()
                                Log.e("DeleteRoomTypeScreen", "Error deleting room type: ${e.message}")
                            }
                        return
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    deletionSuccess = false
                    Toast.makeText(context, "Failed to delete room type", Toast.LENGTH_SHORT).show()
                    Log.e("DeleteRoomTypeScreen", "Error deleting room type: ${databaseError.message}")
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
                    modifier = Modifier.menuAnchor().fillMaxWidth()
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

            // Delete Button
            Button(
                onClick = {
                    if (selectedRoomTypeName.isNotEmpty()) {
                        deleteRoomType(selectedRoomTypeName)
                    } else {
                        Toast.makeText(context, "Please select a room type to delete", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
            ) {
                Text("Delete Room Type")
            }
        }
    }
}