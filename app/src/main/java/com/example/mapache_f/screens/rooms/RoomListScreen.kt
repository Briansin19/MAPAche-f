package com.example.mapache_f.screens.rooms

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
import com.example.mapache_f.classes.Room
import com.example.mapache_f.ui.theme.azulTec
import com.example.mapache_f.ui.theme.naranjaTec
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun RoomListScreen(navController: NavController) {
    var rooms by remember { mutableStateOf<List<Room>>(emptyList()) }
    var isBackButtonEnabled by remember { mutableStateOf(true) }
    val database = FirebaseDatabase.getInstance()
    val roomsRef = database.getReference("rooms")

    val roomListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val roomList = mutableListOf<Room>()
            for (snapshot in dataSnapshot.children) {
                val room = snapshot.getValue(Room::class.java)
                room?.let { roomList.add(it) }
            }
            rooms = roomList
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("RoomListScreen", "loadPost:onCancelled", databaseError.toException())
        }
    }

    LaunchedEffect(Unit) {
        roomsRef.addValueEventListener(roomListener)
    }

    DisposableEffect(Unit) {
        onDispose {
            roomsRef.removeEventListener(roomListener)
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
                    contentDescription = "Atrás",
                    tint = Color.Black
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Listado de Lugares",
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
                    items(rooms) { room ->
                        RoomCard(room)
                    }
                }
            }
        }
    }
}

@Composable
fun RoomCard(room: Room) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "ID: ${room.id}", fontSize = 16.sp, color = azulTec)
            Text(text = "Nombre: ${room.name}", fontSize = 18.sp, color = naranjaTec)
            Text(text = "Descripción: ${room.description}", fontSize = 14.sp, color = Color.Gray)
            Text(text = "ID de Tipo de Sala: ${room.roomTypeId}", fontSize = 14.sp, color = Color.Gray)
            Text(text = "ID del Edificio: ${room.buildingId}", fontSize = 14.sp, color = Color.Gray)
            Text(text = "Número de Piso: ${room.floorNumber}", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Preview
@Composable
fun RoomListScreenPreview() {
    RoomListScreen(navController = rememberNavController())
}
