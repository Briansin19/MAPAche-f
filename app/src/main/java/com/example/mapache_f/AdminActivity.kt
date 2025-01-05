package com.example.mapache_f.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mapache_f.R
import com.example.mapache_f.ui.theme.naranjaTec

@Composable
fun AdminScreen(navController: NavController) {
    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
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

            Spacer(modifier = Modifier.height(32.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AdminButton(
                        navController = navController,
                        route = "buildingMain",
                        text = "Buildings",
                        iconRes = R.drawable.building_solid
                    )
                }
                item {
                    AdminButton(
                        navController = navController,
                        route = "roomMain",
                        text = "Rooms",
                        iconRes = R.drawable.house_solid
                    )
                }
                item {
                    AdminButton(
                        navController = navController,
                        route = "roomTypeMain",
                        text = "Room Types",
                        iconRes = R.drawable.house_circle_exclamation_solid
                    )
                }
                item {
                    AdminButton(
                        navController = navController,
                        route = "eventMain",
                        text = "Events",
                        iconRes = R.drawable.calendar_days_solid
                    )
                }
            }
        }
    }
}

@Composable
fun AdminButton(navController: NavController, route: String, text: String, iconRes: Int) {
    Button(
        onClick = { navController.navigate(route) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = iconRes).apply { tint(Color.White) },
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text)
        }
    }
}

@Preview
@Composable
fun AdminScreenPreview() {
    Surface(color = Color.White) {
        AdminScreen(navController = NavController(LocalContext.current))
    }
}
