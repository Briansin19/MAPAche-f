package com.example.mapache_f

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mapache_f.ui.theme.MAPAchefTheme
import com.example.mapache_f.ui.theme.naranjaTec
import com.example.mapache_f.R
import com.example.mapache_f.screens.AdminScreen
import com.example.mapache_f.screens.buildings.BuildingListScreen
import com.example.mapache_f.screens.buildings.BuildingMainScreen
import com.example.mapache_f.screens.buildings.RegisterBuildingScreen
import com.example.mapache_f.screens.logins.LoginScreen
import com.example.mapache_f.screens.logins.SignUpScreen
import com.example.mapache_f.screens.map.MapScreen
import com.example.mapache_f.screens.map.MapViewModel


class MainActivity : ComponentActivity() {
    private val viewModel: MapViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MAPAchefTheme {
                Surface(color = Color.White) {
                    MyAppNavHost(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MyAppNavHost(viewModel: MapViewModel) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("admin") { AdminScreen(navController) }
        composable("map") { MapScreen(viewModel) }
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }
        composable("buildingMain") { BuildingMainScreen(navController) }
        composable("registerBuilding") { RegisterBuildingScreen(navController) }
        composable("buildingList") { BuildingListScreen(navController) }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { navController.navigate("login") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
        ) {
            Text("Log In")
        }
        Button(
            onClick = { navController.navigate("signup") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
        ) {
            Text("Sign Up")
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MAPAchefTheme {
        Surface(color = Color.White) {
            MainScreen(rememberNavController())
        }
    }
}
