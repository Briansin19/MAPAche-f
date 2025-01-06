package com.example.mapache_f.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mapache_f.R
import com.example.mapache_f.ui.theme.azulTec
import com.example.mapache_f.ui.theme.blancoTec
import com.example.mapache_f.ui.theme.naranjaTec

@Composable
fun AdminScreen(navController: NavController) {
    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
        ) {
            Text(
                text = "Panel de Admin",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(color = blancoTec, shape = RoundedCornerShape(8.dp))
                    .padding(8.dp),
                color = azulTec
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

                Spacer(modifier = Modifier.height(32.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp), // Ajusta la altura segÃºn sea necesario
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        AdminButton(
                            navController = navController,
                            route = "buildingMain",
                            text = "Edificios",
                            iconRes = R.drawable.building_solid
                        )
                    }
                    item {
                        AdminButton(
                            navController = navController,
                            route = "roomMain",
                            text = "Lugares",
                            iconRes = R.drawable.house_solid
                        )
                    }
                    item {
                        AdminButton(
                            navController = navController,
                            route = "roomTypeMain",
                            text = "Tipos de Lugares",
                            iconRes = R.drawable.house_circle_exclamation_solid
                        )
                    }
                    item {
                        AdminButton(
                            navController = navController,
                            route = "eventMain",
                            text = "Eventos",
                            iconRes = R.drawable.calendar_days_solid
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminButton(navController: NavController, route: String, text: String, iconRes: Int) {
    Button(
        onClick = { navController.navigate(route) },
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .width(120.dp)
            .height(150.dp)
            .padding(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = naranjaTec)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                colorFilter = tint(Color.White) // Aplica el filtro de color blanco
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
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