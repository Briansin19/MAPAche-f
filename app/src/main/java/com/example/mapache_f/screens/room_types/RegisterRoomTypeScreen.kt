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
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterRoomTypeScreen(navController: NavController) {
    var roomTypeName by remember { mutableStateOf("") }
    var roomTypeDescription by remember { mutableStateOf("") }
    val context = LocalContext.current

    fun registerRoomType() {
        val name = roomTypeName.trim()
        val description = roomTypeDescription.trim()

        if (name.isNotEmpty() && description.isNotEmpty()) {
            val id = generateUniqueId() // Assuming you have a function to generate unique IDs
            val roomType = RoomTypes(id, name, description)

            val database = FirebaseDatabase.getInstance()
            val roomTypesRef = database.getReference("room_types")

            roomTypesRef.child(id).setValue(roomType)
                .addOnSuccessListener {
                    Log.d("RegisterRoomType", "Room type stored successfully")
                    Toast.makeText(context, "Room type registered successfully", Toast.LENGTH_SHORT).show()
                    navController.navigate("roomTypeMain") // Navigate back to room type buttons screen
                }
                .addOnFailureListener { e ->
                    Log.w("RegisterRoomType", "Error storing Room type", e)
                    Toast.makeText(context, "Failed to register room type", Toast.LENGTH_SHORT).show()
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
                value = roomTypeName,
                onValueChange = { roomTypeName = it },
                label = { Text("Room Type Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            OutlinedTextField(
                value = roomTypeDescription,
                onValueChange = { roomTypeDescription = it },
                label = { Text("Room Type Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            Button(
                onClick = { registerRoomType() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
            ) {
                Text("Register Room Type")
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