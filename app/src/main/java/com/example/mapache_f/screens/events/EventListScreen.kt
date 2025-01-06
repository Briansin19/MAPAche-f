package com.example.mapache_f.screens.events

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mapache_f.R
import com.example.mapache_f.classes.Events
import com.example.mapache_f.ui.theme.azulTec
import com.example.mapache_f.ui.theme.naranjaTec
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun EventListScreen(navController: NavController) {
    var events by remember { mutableStateOf<List<Events>>(emptyList()) }
    var isBackButtonEnabled by remember { mutableStateOf(true) }
    val database = FirebaseDatabase.getInstance()
    val eventsRef = database.getReference("events")

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

    Surface() {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
        ) {
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
                    .padding(16.dp)
            ) {
                Text(
                    text = "Listado de Eventos",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = azulTec,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(events) { event ->
                        EventCard(event)
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: Events) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "ID: ${event.id}", fontSize = 16.sp, color = azulTec)
            Text(text = "Nombre: ${event.name}", fontSize = 18.sp, color = naranjaTec)
            Text(text = "Descripción: ${event.description}", fontSize = 14.sp, color = Color.Gray)
            Text(text = "Hora de Inicio: ${event.startHour}", fontSize = 14.sp, color = Color.Gray)
            Text(text = "Fecha de Inicio: ${event.startDate}", fontSize = 14.sp, color = Color.Gray)
            Text(text = "Fecha de Fin: ${event.endDate}", fontSize = 14.sp, color = Color.Gray)
            Text(text = "ID de Sala: ${event.roomId}", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Preview
@Composable
fun EventListScreenPreview() {
    EventListScreen(navController = rememberNavController())
}