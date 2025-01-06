package com.example.mapache_f.screens.roomTypes

import android.util.Log
import android.widget.Toast
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
import com.example.mapache_f.classes.RoomTypes
import com.example.mapache_f.ui.theme.naranjaTec
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterRoomTypeScreen(navController: NavController) {
    var roomTypeName by remember { mutableStateOf("") }
    var roomTypeDescription by remember { mutableStateOf("") }
    val context = LocalContext.current

    fun registerRoomType() {
        val name = roomTypeName.trim()
        val description = roomTypeDescription.trim()

        if (name.isNotEmpty() && description.isNotEmpty()) {
            val id = generateUniqueId()
            val roomType = RoomTypes(id, name, description)

            val database = FirebaseDatabase.getInstance()
            val roomTypesRef = database.getReference("room_types")

            roomTypesRef.child(id).setValue(roomType)
                .addOnSuccessListener {
                    Log.d("RegisterRoomType", "Tipo de sala registrado exitosamente")
                    Toast.makeText(context, "Tipo de sala registrado exitosamente", Toast.LENGTH_SHORT).show()
                    navController.navigate("roomTypeMain")
                }
                .addOnFailureListener { e ->
                    Log.w("RegisterRoomType", "Error al registrar el tipo de sala", e)
                    Toast.makeText(context, "Error al registrar el tipo de sala", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(color = Color.White, modifier = Modifier
        .fillMaxSize()
        .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Registrar Tipo de Sala",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = roomTypeName,
                onValueChange = { roomTypeName = it },
                label = { Text("Nombre del Tipo de Sala") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            OutlinedTextField(
                value = roomTypeDescription,
                onValueChange = { roomTypeDescription = it },
                label = { Text("DescripciÃ³n del Tipo de Sala") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            Button(
                onClick = { registerRoomType() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
            ) {
                Text("Registrar Tipo de Sala")
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