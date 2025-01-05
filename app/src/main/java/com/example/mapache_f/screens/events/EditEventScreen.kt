package com.example.mapache_f.screens.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mapache_f.R
import com.example.mapache_f.classes.Events
import com.example.mapache_f.ui.theme.naranjaTec
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar
import kotlin.text.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(navController: NavController) {
    var selectedEventName by remember { mutableStateOf("") }
    var selectedEventId by remember { mutableStateOf("") }
    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var eventStartHour by remember { mutableStateOf("") }
    var eventStartDate by remember { mutableStateOf("") }
    var eventEndDate by remember { mutableStateOf("") }
    var selectedRoomId by remember { mutableStateOf("") }
    var roomNames by remember { mutableStateOf(listOf<String>()) }
    var roomIdMap by remember { mutableStateOf(mapOf<String, String>()) }
    var eventNames by remember { mutableStateOf(listOf<String>()) }
    var updateSuccess by remember { mutableStateOf(false) }

    val context = LocalContext.current

    //variables para el manejo de datos de habitaciones
    val databaseRoom = FirebaseDatabase.getInstance()
    val roomsRef = databaseRoom.getReference("rooms")

    val roomListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val names = mutableListOf<String>()
            val idMap = mutableMapOf<String, String>()
            for (snapshot in dataSnapshot.children) {
                val roomName = snapshot.child("name").getValue(String::class.java)
                val roomId = snapshot.key
                if (roomName != null && roomId != null) {
                    names.add(roomName)
                    idMap[roomName] = roomId
                }
            }
            roomNames = names
            roomIdMap = idMap
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("EditEventScreen", "Error getting rooms", databaseError.toException())
        }
    }

    //variables para el manejo de datos de eventos
    val databaseEvent = FirebaseDatabase.getInstance()
    val eventsRef = databaseEvent.getReference("events")

    val eventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val names = mutableListOf<String>()
            for (snapshot in dataSnapshot.children) {
                val event = snapshot.getValue(Events::class.java)
                event?.name?.let { names.add(it) }
            }
            eventNames = names
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.e("EditEventScreen", "Error fetching event names: ${databaseError.message}")
        }
    }

    val datePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                val day = intent.getIntExtra( CalendarContract.EXTRA_EVENT_BEGIN_TIME, 0)
                val month = intent.getIntExtra(CalendarContract.EXTRA_EVENT_END_TIME, 0)
                val year = intent.getIntExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, 0)
                eventStartDate = "$day/${month + 1}/$year" // Update eventStartDate state
            }
        }
    }

    val timePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                val hourOfDay = intent.getIntExtra(CalendarContract.EXTRA_EVENT_ID, 0)
                val minute = intent.getIntExtra(CalendarContract.Instances.EVENT_ID, 0)
                eventStartHour = String.format("%02d:%02d", hourOfDay, minute) // Update eventStartHour state
            }
        }
    }

    // Fetch event names from Firebase
    LaunchedEffect(Unit) {
        eventsRef.addValueEventListener(eventListener)
    }

    DisposableEffect(Unit) {
        onDispose {
            eventsRef.removeEventListener(eventListener)
        }
    }

    // Fetch room names from Firebase
    LaunchedEffect(Unit) {
        roomsRef.addValueEventListener(roomListener)
    }

    DisposableEffect(Unit) {
        onDispose {
            roomsRef.removeEventListener(roomListener)
        }
    }

    // Fetch selected event data
    LaunchedEffect(selectedEventName) {
        if (selectedEventName.isNotEmpty()) {
            val database = FirebaseDatabase.getInstance()
            database.getReference("events").orderByChild("name").equalTo(selectedEventName)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val event = snapshot.getValue(Events::class.java)
                            event?.let {
                                eventName = it.name
                                eventDescription = it.description
                                eventStartHour = it.startHour
                                eventStartDate = it.startDate
                                eventEndDate = it.endDate
                                selectedRoomId = it.roomId
                                selectedEventId = it.id

                                // Set room spinner selection
                                val roomName = roomIdMap.entries.find { it.value == selectedRoomId }?.key
                                roomName?.let { selectedRoomName ->
                                    val roomPosition = roomNames.indexOf(selectedRoomName)
                                    // You'll need to adapt this part to update the selected room in your UI
                                    // For example, if you're using an ExposedDropdownMenuBox, you can update the selectedRoomId state
                                    selectedRoomId = selectedRoomName
                                }
                            }
                            return
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("EditEventScreen", "Error getting selected event: ${databaseError.message}")
                    }
                })
        }
    }

    // Update event in Firebase
    fun updateEvent() {
        val roomId = roomIdMap[selectedRoomId] // Get roomId from selected room name

        if (eventName.isNotEmpty() && eventDescription.isNotEmpty() && eventStartHour.isNotEmpty() && eventStartDate.isNotEmpty() && eventEndDate.isNotEmpty() && roomId != null) {
            val updatedEvent = Events(
                id = selectedEventId, // Assuming selectedEventName is the event ID
                name = eventName,
                description = eventDescription,
                startHour = eventStartHour,
                startDate = eventStartDate,
                endDate = eventEndDate,
                roomId = roomId
            )

            val database = FirebaseDatabase.getInstance()
            database.getReference("events").child(selectedEventId).setValue(updatedEvent)
                .addOnSuccessListener {
                    updateSuccess = true
                    Toast.makeText(context, "Event updated successfully", Toast.LENGTH_SHORT).show()
                    navController.navigate("eventMain")
                }
                .addOnFailureListener { e ->
                    updateSuccess = false
                    Toast.makeText(context, "Failed to update event", Toast.LENGTH_SHORT).show()
                    Log.e("EditEventScreen", "Error updating event: ${e.message}")
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
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
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

            // Event Name
            OutlinedTextField(
                value = eventName,
                onValueChange = { eventName = it },
                label = { Text("Event Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            // Event Description
            OutlinedTextField(
                value = eventDescription,
                onValueChange = { eventDescription = it },
                label = { Text("Event Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            var expandedRoomSpinner by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedRoomSpinner,
                onExpandedChange = { expandedRoomSpinner = !expandedRoomSpinner }
            ) {
                TextField(
                    value = selectedRoomId,
                    onValueChange = { selectedRoomId = it },
                    readOnly = true,
                    label = { Text("Room") },
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
                                selectedRoomId = roomName
                                expandedRoomSpinner = false
                            }
                        )
                    }
                }
            }

            // Event Start Hour
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = eventStartHour,
                    onValueChange = { eventStartHour = it },
                    label = { Text("Start Hour") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    readOnly = true,
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

            // Event Start Date
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = eventStartDate,
                    onValueChange = { eventStartDate = it },
                    label = { Text("Start Date") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    readOnly = true,
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

            // Event End Date
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = eventEndDate,
                    onValueChange = { eventEndDate = it },
                    label = { Text("End Date") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    readOnly = true,
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

            // Update Button
            Button(
                onClick = { updateEvent() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
            ) {
                Text("Update Event")
            }
        }
    }
}