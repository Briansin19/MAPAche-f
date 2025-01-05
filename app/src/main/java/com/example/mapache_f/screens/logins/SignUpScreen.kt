package com.example.mapache_f.screens.logins

import android.widget.Toast
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mapache_f.R
import com.example.mapache_f.classes.UserInfo
import com.example.mapache_f.ui.theme.blancoTec
import com.example.mapache_f.ui.theme.naranjaTec
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    fun handleSignUp() {
        if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
            if (password == confirmPassword) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val userInfo = UserInfo(
                                id = user!!.uid,
                                email = email,
                                password = password,
                                admin = false
                            )

                            val database = FirebaseDatabase.getInstance()
                            val usersRef = database.getReference("users")

                            usersRef.child(user.uid).setValue(userInfo)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Sign up successful!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("login") // Navigate to login screen
                                }
                                .addOnFailureListener { e ->
                                    Log.w("SignUpError", "Error storing user info", e)
                                    Toast.makeText(context, "Failed to store user info", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Log.w("SignUpError", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(context, task.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(color = Color.White) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Registro",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(color = blancoTec, shape = RoundedCornerShape(8.dp))
                    .padding(8.dp),
                color = Color.Black
            )
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

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

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

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
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

                Button(
                    onClick = { handleSignUp() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
                ) {
                    Text("Sign Up")
                }

                TextButton(
                    onClick = { navController.popBackStack() }, // Navigate back to login
                    modifier = Modifier
                        .padding(16.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = naranjaTec)
                ) {
                    Text("Already have an account? Log in")
                }
            }
        }
    }
}