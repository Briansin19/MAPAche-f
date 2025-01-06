package com.example.mapache_f.screens.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mapache_f.R
import com.example.mapache_f.classes.Events
import com.example.mapache_f.ui.theme.azulTec
import com.example.mapache_f.ui.theme.naranjaTec
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterEventScreen(navController: NavController) {
    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var eventStartHour by remember { mutableStateOf("") }
    var eventStartDate by remember { mutableStateOf("") }
    var eventEndDate by remember { mutableStateOf("") }
    var selectedRoomId by remember { mutableStateOf("") }
    var roomNames by remember { mutableStateOf(listOf<String>()) }
    var expanded by remember { mutableStateOf(false) }
    var isBackButtonEnabled by remember { mutableStateOf(true) }
    val database = FirebaseDatabase.getInstance()
    val roomsRef = database.getReference("rooms")

    val roomListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val names = mutableListOf<String>()
            for (snapshot in dataSnapshot.children) {
                val roomName = snapshot.child("name").getValue(String::class.java)
                roomName?.let { names.add(it) }
            }
            roomNames = names
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("RegisterEventScreen", "Error getting rooms", databaseError.toException())
        }
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        roomsRef.addValueEventListener(roomListener)
    }

    DisposableEffect(Unit) {
        onDispose {
            roomsRef.removeEventListener(roomListener)
        }
    }

    fun registerEvent() {
        val name = eventName.trim()
        val description = eventDescription.trim()
        val startHour = eventStartHour.trim()
        val startDate = eventStartDate.trim()
        val endDate = eventEndDate.trim()
        val roomId = selectedRoomId

        if (name.isNotEmpty() && description.isNotEmpty() && startHour.isNotEmpty() && startDate.isNotEmpty() && endDate.isNotEmpty() && roomId.isNotEmpty()) {
            val id = generateUniqueId()
            val event = Events(id, name, description, startHour, startDate, endDate, roomId)

            val eventsRef = database.getReference("events")

            eventsRef.child(id).setValue(event)
                .addOnSuccessListener {
                    Log.d("RegisterEvent", "Event stored successfully")
                    Toast.makeText(context, "Event registered successfully", Toast.LENGTH_SHORT).show()
                    navController.navigate("eventMain")
                }
                .addOnFailureListener { e ->
                    Log.w("RegisterEvent", "Error storing Event", e)
                    Toast.makeText(context, "Failed to register event", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
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
                    contentDescription = "Back",
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
                    text = "Registrar Evento",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = azulTec,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("Nombre del Evento") },
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
                    value = eventDescription,
                    onValueChange = { eventDescription = it },
                    label = { Text("Descripción del Evento") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = naranjaTec,
                        unfocusedBorderColor = azulTec,
                        cursorColor = naranjaTec
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = eventStartHour,
                        onValueChange = { eventStartHour = it },
                        label = { Text("Hora de Inicio") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        readOnly = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = naranjaTec,
                            unfocusedBorderColor = azulTec,
                            cursorColor = naranjaTec
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    )
                    IconButton(onClick = {
                        val calendar = Calendar.getInstance()
                        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                        val currentMinute = calendar.get(Calendar.MINUTE)

                        val timePickerDialog = TimePickerDialog(
                            context,
                            { _: TimePicker, hourOfDay: Int, minute: Int ->
                                eventStartHour = String.format("%02d:%02d", hourOfDay, minute)
                            },
                            currentHour,
                            currentMinute,
                            true
                        )
                        timePickerDialog.show()
                    }) {
                        Image(
                            painter = painterResource(id = R.drawable.schedule),
                            contentDescription = "Seleccione la Hora"
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = eventStartDate,
                        onValueChange = { eventStartDate = it },
                        label = { Text("Fecha de Inicio") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        readOnly = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = naranjaTec,
                            unfocusedBorderColor = azulTec,
                            cursorColor = naranjaTec
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    )
                    IconButton(onClick = {
                        val calendar = Calendar.getInstance()
                        val currentYear = calendar.get(Calendar.YEAR)
                        val currentMonth = calendar.get(Calendar.MONTH)
                        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

                        val datePickerDialog = DatePickerDialog(
                            context,
                            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                                eventStartDate = "$dayOfMonth/${month + 1}/$year"
                            },
                            currentYear,
                            currentMonth,
                            currentDay
                        )
                        datePickerDialog.show()
                    }) {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_calendar_today_24),
                            contentDescription = "Seleccione la Fecha De Inicio"
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = eventEndDate,
                        onValueChange = { eventEndDate = it },
                        label = { Text("Fecha de Fin") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        readOnly = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = naranjaTec,
                            unfocusedBorderColor = azulTec,
                            cursorColor = naranjaTec
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    )
                    IconButton(onClick = {
                        val calendar = Calendar.getInstance()
                        val currentYear = calendar.get(Calendar.YEAR)
                        val currentMonth = calendar.get(Calendar.MONTH)
                        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

                        val datePickerDialog = DatePickerDialog(
                            context,
                            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                                eventEndDate = "$dayOfMonth/${month + 1}/$year"
                            },
                            currentYear,
                            currentMonth,
                            currentDay
                        )
                        datePickerDialog.show()
                    }) {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_calendar_today_24),
                            contentDescription = "Seleccione la Fecha De Finalización"
                        )
                    }
                }

                ExposedDropdownMenuBox(
                    modifier = Modifier.padding(8.dp),
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = selectedRoomId,
                        onValueChange = { selectedRoomId = it },
                        readOnly = true,
                        label = { Text("Sala") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roomNames.forEach { roomName ->
                            DropdownMenuItem(
                                text = { Text(roomName) },
                                onClick = {
                                    selectedRoomId = roomName
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = { registerEvent() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
                ) {
                    Text("Registrar Evento", color = Color.White)
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
