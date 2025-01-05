package com.example.mapache_f.screens.rooms

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
import com.example.mapache_f.classes.Room // Import your Room data class
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun RoomListScreen(navController: NavController) {
    var rooms by remember { mutableStateOf<List<Room>>(emptyList()) }
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

    Surface(color = Color.White) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(rooms) { room ->
                RoomItem(room)
            }
        }
    }
}

@Composable
fun RoomItem(room: Room) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text("ID: ${room.id}")
        Text("Name: ${room.name}")
        Text("Description: ${room.description}")
        Text("Room Type ID: ${room.roomTypeId}")
        Text("Building ID: ${room.buildingId}")
        Text("Floor Number: ${room.floorNumber}")
        Spacer(modifier = Modifier.height(8.dp))
    }
}