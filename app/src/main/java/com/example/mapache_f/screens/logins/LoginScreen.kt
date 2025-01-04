package com.example.mapache_f.screens.logins

import android.widget.Toast
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mapache_f.R
import com.example.mapache_f.ui.theme.naranjaTec
import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.UserInfo
import com.google.firebase.database.FirebaseDatabase
import com.example.mapache_f.classes.UserInfo


@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    fun handleLogin(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        Log.d("LoginSuccess", "User logged in: ${user?.email}")

                        // Accede a la base de datos
                        val database = FirebaseDatabase.getInstance()
                        val usersRef = database.getReference("users")
                        usersRef.child(user?.uid.toString()).get()
                            .addOnSuccessListener { dataSnapshot ->
                                if (dataSnapshot.exists()) {
                                    val userInfo = dataSnapshot.getValue(UserInfo::class.java)
                                    if (userInfo?.admin == true) {
                                        navController.navigate("admin") // Pantalla para administradores
                                    } else {
                                        navController.navigate("map") // Pantalla para usuarios normales
                                    }
                                } else {
                                    Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirebaseError", "Error retrieving user info", e)
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Log.w("LoginError", "signInWithEmail:failure", task.exception)
                        Toast.makeText(context, task.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(context, "Please fill in both fields", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                alignment = Alignment.Center
            )

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        painterResource(id = R.drawable.eye_solid)
                    else
                        painterResource(id = R.drawable.eye_slash_solid)

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(painter = image, contentDescription = null)
                    }
                }
            )

            // Login Button
            Button(
                onClick = { handleLogin(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
            ) {
                Text("Acceder")
            }

            // Sign Up Button
            TextButton(
                onClick = { navController.navigate("signup") },
                modifier = Modifier
                    .padding(16.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = naranjaTec)
            ) {
                Text("No tengo una cuenta")
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}
