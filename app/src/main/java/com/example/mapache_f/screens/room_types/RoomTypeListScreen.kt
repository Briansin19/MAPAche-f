package com.example.mapache_f.screens.roomTypes

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
import com.example.mapache_f.classes.RoomTypes
import com.example.mapache_f.ui.theme.azulTec
import com.example.mapache_f.ui.theme.naranjaTec
import com.example.mapache_f.ui.theme.blancoTec
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun RoomTypeListScreen(navController: NavController) {
    var roomTypes by remember { mutableStateOf<List<RoomTypes>>(emptyList()) }
    var isBackButtonEnabled by remember { mutableStateOf(true) }
    val database = FirebaseDatabase.getInstance()
    val roomTypesRef = database.getReference("room_types")

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
                    text = "Listado de tipos de Lugares",
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
                    items(roomTypes) { roomType ->
                        RoomTypeCard(roomType)
                    }
                }
            }
        }
    }
}

@Composable
fun RoomTypeCard(roomType: RoomTypes) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "ID: ${roomType.id}", fontSize = 16.sp, color = azulTec)
            Text(text = "Nombre: ${roomType.name}", fontSize = 18.sp, color = naranjaTec)
            Text(text = "DescripciÃ³n: ${roomType.description}", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Preview
@Composable
fun RoomTypeListScreenPreview() {
    RoomTypeListScreen(navController = rememberNavController())
}