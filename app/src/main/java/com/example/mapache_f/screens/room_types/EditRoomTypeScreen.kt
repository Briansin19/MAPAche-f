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
import com.example.mapache_f.ui.theme.azulTec
import com.example.mapache_f.ui.theme.naranjaTec
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoomTypeScreen(navController: NavController) {
    var selectedRoomTypeName by remember { mutableStateOf("") }
    var selectedRoomTypeId by remember { mutableStateOf("") }
    var roomTypeName by remember { mutableStateOf("") }
    var roomTypeDescription by remember { mutableStateOf("") }
    var roomTypeNames by remember { mutableStateOf(listOf<String>()) }
    var updateSuccess by remember { mutableStateOf(false) }
    var isBackButtonEnabled by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance()

    LaunchedEffect(Unit) {
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
                Log.e("EditRoomTypeScreen", "Error fetching room type names: ${databaseError.message}")
            }
        })
    }

    LaunchedEffect(selectedRoomTypeName) {
        if (selectedRoomTypeName.isNotEmpty()) {
            database.getReference("room_types").orderByChild("name").equalTo(selectedRoomTypeName)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val roomType = snapshot.getValue(RoomTypes::class.java)
                            roomType?.let {
                                selectedRoomTypeId = it.id
                                roomTypeName = it.name
                                roomTypeDescription = it.description
                            }
                            return
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("EditRoomTypeScreen", "Error getting selected room type: ${databaseError.message}")
                    }
                })
        }
    }

    fun updateRoomType() {
        if (roomTypeName.isNotEmpty() && roomTypeDescription.isNotEmpty()) {
            val updatedRoomType = RoomTypes(
                id = selectedRoomTypeId,
                name = roomTypeName,
                description = roomTypeDescription
            )

            database.getReference("room_types").child(selectedRoomTypeId).setValue(updatedRoomType)
                .addOnSuccessListener {
                    updateSuccess = true
                    Toast.makeText(context, "Tipo de lugar actualizado exitosamente", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
                .addOnFailureListener { e ->
                    updateSuccess = false
                    Toast.makeText(context, "Error al actualizar el tipo de lugar", Toast.LENGTH_SHORT).show()
                    Log.e("EditRoomTypeScreen", "Error updating room type: ${e.message}")
                }
        } else {
            Toast.makeText(context, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
        }
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
                    contentDescription = "Atrás",
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
                    text = "Editar Tipo de Lugar",
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

                OutlinedTextField(
                    value = roomTypeName,
                    onValueChange = { roomTypeName = it },
                    label = { Text("Nombre del Tipo de Lugar") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = naranjaTec,
                        unfocusedBorderColor = azulTec,
                        cursorColor = naranjaTec
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                OutlinedTextField(
                    value = roomTypeDescription,
                    onValueChange = { roomTypeDescription = it },
                    label = { Text("Descripción del Tipo de Lugar") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = naranjaTec,
                        unfocusedBorderColor = azulTec,
                        cursorColor = naranjaTec
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                Button(
                    onClick = { updateRoomType() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
                ) {
                    Text("Actualizar Tipo de Lugar", color = Color.White)
                }
            }
        }
    }
}