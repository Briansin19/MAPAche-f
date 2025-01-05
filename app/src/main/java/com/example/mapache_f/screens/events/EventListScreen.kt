package com.example.mapache_f.screens.events

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mapache_f.classes.Events
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun EventListScreen(navController: NavController) {
    var events by remember { mutableStateOf<List<Events>>(emptyList()) }
    val database = FirebaseDatabase.getInstance()
    val eventsRef = database.getReference("events") // Change to "events"

    val eventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val eventList = mutableListOf<Events>()
            for (snapshot in dataSnapshot.children) {
                val event = snapshot.getValue(Events::class.java)
                event?.let { eventList.add(it) }
            }
            events = eventList
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("EventListScreen", "loadPost:onCancelled", databaseError.toException())
        }
    }

    LaunchedEffect(Unit) {
        eventsRef.addValueEventListener(eventListener)
    }

    DisposableEffect(Unit) {
        onDispose {
            eventsRef.removeEventListener(eventListener)
        }
    }

    Surface(color = Color.White) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(events) { event ->
                EventItem(event) // Use EventItem composable
            }
        }
    }
}

@Composable
fun EventItem(event: Events) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text("ID: ${event.id}")
        Text("Name: ${event.name}")
        Text("Description: ${event.description}")
        Text("Start Hour: ${event.startHour}")
        Text("Start Date: ${event.startDate}")
        Text("End Date: ${event.endDate}")
        Text("Room ID: ${event.roomId}")
        Spacer(modifier = Modifier.height(8.dp)) // Add spacing between items
    }
}