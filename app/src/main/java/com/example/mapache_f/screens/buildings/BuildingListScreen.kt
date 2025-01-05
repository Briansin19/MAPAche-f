package com.example.mapache_f.screens.buildings

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mapache_f.classes.Building
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun BuildingListScreen(navController: NavController) {
    var buildings by remember { mutableStateOf<List<Building>>(emptyList()) }
    val database = FirebaseDatabase.getInstance()
    val buildingsRef = database.getReference("buildings")

    val buildingListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val buildingList = mutableListOf<Building>()
            for (snapshot in dataSnapshot.children) {
                val building = snapshot.getValue(Building::class.java)
                building?.let { buildingList.add(it) }
            }
            buildings = buildingList
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("BuildingListScreen", "loadPost:onCancelled", databaseError.toException())
        }
    }

    LaunchedEffect(Unit) {
        buildingsRef.addValueEventListener(buildingListener)
    }

    DisposableEffect(Unit) {
        onDispose {
            buildingsRef.removeEventListener(buildingListener)
        }
    }

    Surface(color = Color.White) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(buildings) { building ->
                BuildingItem(building)
            }
        }
    }
}

@Composable
fun BuildingItem(building: Building) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text("ID: ${building.id}")
        Text("Name: ${building.name}")
        Text("Description: ${building.description}")
        Text("Latitude: ${building.latitude}")
        Text("Longitude: ${building.longitude}")
        Spacer(modifier = Modifier.height(8.dp)) // Add spacing between items
    }
}