package com.example.mapache_f.screens.events

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
fun DeleteEventScreen(navController: NavController) {
    var selectedEventName by remember { mutableStateOf("") }
    var eventNames by remember { mutableStateOf(listOf<String>()) }
    var deletionSuccess by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Fetch event names from Firebase
    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
        database.getReference("events").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val names = mutableListOf<String>()
                for (snapshot in dataSnapshot.children) {
                    val eventName = snapshot.child("name").getValue(String::class.java)
                    eventName?.let { names.add(it) }
                }
                eventNames = names
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DeleteEventScreen", "Error fetching event names: ${databaseError.message}")
            }
        })
    }

    // Delete event from Firebase
    fun deleteEvent(eventName: String) {
        val database = FirebaseDatabase.getInstance()
        database.getReference("events").orderByChild("name").equalTo(eventName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        snapshot.ref.removeValue()
                            .addOnSuccessListener {
                                deletionSuccess = true
                                Toast.makeText(context, "Event deleted successfully", Toast.LENGTH_SHORT).show()
                                navController.popBackStack() // Navigate to the previous screen
                            }
                            .addOnFailureListener { e ->
                                deletionSuccess = false
                                Toast.makeText(context, "Failed to delete event", Toast.LENGTH_SHORT).show()
                                Log.e("DeleteEventScreen", "Error deleting event: ${e.message}")
                            }
                        return
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    deletionSuccess = false
                    Toast.makeText(context, "Failed to delete event", Toast.LENGTH_SHORT).show()
                    Log.e("DeleteEventScreen", "Error deleting event: ${databaseError.message}")
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
            // Event Spinner
            var expandedEventSpinner by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedEventSpinner,
                onExpandedChange = { expandedEventSpinner = !expandedEventSpinner }
            ) {
                TextField(
                    value = selectedEventName,
                    onValueChange = { selectedEventName = it },
                    readOnly = true,
                    label = { Text("Select Event") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEventSpinner) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedEventSpinner,
                    onDismissRequest = { expandedEventSpinner = false }
                ) {
                    eventNames.forEach { eventName ->
                        DropdownMenuItem(
                            text = { Text(eventName) },
                            onClick = {
                                selectedEventName = eventName
                                expandedEventSpinner = false
                            }
                        )
                    }
                }
            }

            // Delete Button
            Button(
                onClick = {
                    if (selectedEventName.isNotEmpty()) {
                        deleteEvent(selectedEventName)
                    } else {
                        Toast.makeText(context, "Please select an event to delete", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
            ) {
                Text("Delete Event")
            }
        }
    }
}