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
import com.example.mapache_f.ui.theme.azulTec
import com.example.mapache_f.ui.theme.naranjaTec
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteRoomTypeScreen(navController: NavController) {
    var selectedRoomTypeName by remember { mutableStateOf("") }
    var roomTypeNames by remember { mutableStateOf(listOf<String>()) }
    var deletionSuccess by remember { mutableStateOf(false) }
    var isBackButtonEnabled by remember { mutableStateOf(true) }

    val context = LocalContext.current

    // Obtener nombres de tipos de lugar desde Firebase
    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
        database.getReference("room_types").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val names = mutableListOf<String>()
                for (snapshot in dataSnapshot.children) {
                    val roomTypeName = snapshot.child("name").getValue(String::class.java)
                    roomTypeName?.let { names.add(it) }
                }
                roomTypeNames = names
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DeleteRoomTypeScreen", "Error al obtener nombres de tipos de lugar: ${databaseError.message}")
            }
        })
    }

    // Eliminar tipo de lugar desde Firebase
    fun deleteRoomType(roomTypeName: String) {
        val database = FirebaseDatabase.getInstance()
        database.getReference("room_types").orderByChild("name").equalTo(roomTypeName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        snapshot.ref.removeValue()
                            .addOnSuccessListener {
                                deletionSuccess = true
                                Toast.makeText(context, "Tipo de lugar eliminado exitosamente", Toast.LENGTH_SHORT).show()
                                navController.popBackStack() // Navegar a la pantalla anterior
                            }
                            .addOnFailureListener { e ->
                                deletionSuccess = false
                                Toast.makeText(context, "Error al eliminar el tipo de lugar", Toast.LENGTH_SHORT).show()
                                Log.e("DeleteRoomTypeScreen", "Error al eliminar el tipo de lugar: ${e.message}")
                            }
                        return
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    deletionSuccess = false
                    Toast.makeText(context, "Error al eliminar el tipo de lugar", Toast.LENGTH_SHORT).show()
                    Log.e("DeleteRoomTypeScreen", "Error al eliminar el tipo de lugar: ${databaseError.message}")
                }
            })
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
                    contentDescription = "AtrÃ¡s",
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
                    text = "Eliminar Tipo de Lugar",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = azulTec,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                )

                var expandedRoomTypeSpinner by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedRoomTypeSpinner,
                    onExpandedChange = { expandedRoomTypeSpinner = !expandedRoomTypeSpinner }
                ) {
                    TextField(
                        value = selectedRoomTypeName,
                        onValueChange = { selectedRoomTypeName = it },
                        readOnly = true,
                        label = { Text("Seleccionar Tipo de Lugar") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoomTypeSpinner) },
                        colors = TextFieldDefaults.textFieldColors(
                            focusedIndicatorColor = naranjaTec,
                            unfocusedIndicatorColor = azulTec,
                            cursorColor = naranjaTec
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedRoomTypeSpinner,
                        onDismissRequest = { expandedRoomTypeSpinner = false }
                    ) {
                        roomTypeNames.forEach { roomTypeName ->
                            DropdownMenuItem(
                                text = { Text(roomTypeName) },
                                onClick = {
                                    selectedRoomTypeName = roomTypeName
                                    expandedRoomTypeSpinner = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (selectedRoomTypeName.isNotEmpty()) {
                            deleteRoomType(selectedRoomTypeName)
                        } else {
                            Toast.makeText(context, "Por favor, seleccione un tipo de lugar para eliminar", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
                ) {
                    Text("Eliminar Tipo de Lugar", color = Color.White)
                }
            }
        }
    }
}
