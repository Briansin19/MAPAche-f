package com.example.mapache_f.screens.events

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mapache_f.R
import com.example.mapache_f.ui.theme.azulTec
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
    var expandedEventSpinner by remember { mutableStateOf(false) }
    var isBackButtonEnabled by remember { mutableStateOf(true) }

    val context = LocalContext.current

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

    fun deleteEvent(eventName: String) {
        val database = FirebaseDatabase.getInstance()
        database.getReference("events").orderByChild("name").equalTo(eventName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        snapshot.ref.removeValue()
                            .addOnSuccessListener {
                                deletionSuccess = true
                                Toast.makeText(context, "Evento eliminado exitosamente", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                            .addOnFailureListener { e ->
                                deletionSuccess = false
                                Toast.makeText(context, "Error al eliminar el evento", Toast.LENGTH_SHORT).show()
                                Log.e("DeleteEventScreen", "Error deleting event: ${e.message}")
                            }
                        return
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    deletionSuccess = false
                    Toast.makeText(context, "Error al eliminar el evento", Toast.LENGTH_SHORT).show()
                    Log.e("DeleteEventScreen", "Error deleting event: ${databaseError.message}")
                }
            })
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
                    text = "Eliminar Evento",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = azulTec,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                )

                ExposedDropdownMenuBox(
                    modifier = Modifier.padding(8.dp),
                    expanded = expandedEventSpinner,
                    onExpandedChange = { expandedEventSpinner = !expandedEventSpinner }
                ) {
                    TextField(
                        value = selectedEventName,
                        onValueChange = { selectedEventName = it },
                        readOnly = true,
                        label = { Text("Seleccionar Evento") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEventSpinner) },
                        colors = TextFieldDefaults.textFieldColors(
                            focusedIndicatorColor = naranjaTec,
                            unfocusedIndicatorColor = azulTec,
                            cursorColor = naranjaTec
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
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

                Button(
                    onClick = {
                        if (selectedEventName.isNotEmpty()) {
                            deleteEvent(selectedEventName)
                        } else {
                            Toast.makeText(context, "Por favor seleccione un evento para eliminar", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
                ) {
                    Text("Eliminar Evento", color = Color.White)
                }
            }
        }
    }
}
