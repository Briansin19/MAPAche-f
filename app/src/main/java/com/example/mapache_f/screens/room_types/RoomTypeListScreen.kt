package com.example.mapache_f.screens.roomTypes

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
import com.example.mapache_f.classes.RoomTypes // Import your RoomTypes data class
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun RoomTypeListScreen(navController: NavController) {
    var roomTypes by remember { mutableStateOf<List<RoomTypes>>(emptyList()) }
    val database = FirebaseDatabase.getInstance()
    val roomTypesRef = database.getReference("room_types") // Change to "room_types"

    val roomTypeListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val roomTypeList = mutableListOf<RoomTypes>()
            for (snapshot in dataSnapshot.children) {
                val roomType = snapshot.getValue(RoomTypes::class.java)
                roomType?.let { roomTypeList.add(it) }
            }
            roomTypes = roomTypeList
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("RoomTypeListScreen", "loadPost:onCancelled", databaseError.toException())
        }
    }

    LaunchedEffect(Unit) {
        roomTypesRef.addValueEventListener(roomTypeListener)
    }

    DisposableEffect(Unit) {
        onDispose {
            roomTypesRef.removeEventListener(roomTypeListener)
        }
    }

    Surface(color = Color.White) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(roomTypes) { roomType ->
                RoomTypeItem(roomType) // Use RoomTypeItem composable
            }
        }
    }
}

@Composable
fun RoomTypeItem(roomType: RoomTypes) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text("ID: ${roomType.id}")
        Text("Name: ${roomType.name}")
        Text("Description: ${roomType.description}")
        Spacer(modifier = Modifier.height(8.dp)) // Add spacing between items
    }
}