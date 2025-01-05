package com.example.mapache_f.screens.buildings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mapache_f.R
import com.example.mapache_f.screens.map.BuildingEntity
import com.example.mapache_f.screens.map.MyApplication
import com.example.mapache_f.ui.theme.azulTec
import com.example.mapache_f.ui.theme.blancoTec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun BuildingListScreen(navController: NavController) {
    var buildings by remember { mutableStateOf<List<BuildingEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val buildingList = MyApplication.database.buildingDao().getAllBuildings()
            buildings = buildingList
        }
    }

    Surface(color = Color.White) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Edificios recuperados",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
                    .background(color = blancoTec, shape = RoundedCornerShape(8.dp))
                    .padding(8.dp),
                color = Color.Black
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
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
}

@Composable
fun BuildingItem(building: BuildingEntity) {
    Card(
        shape = CardDefaults.shape,
        colors = CardDefaults.cardColors(containerColor = azulTec),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.building_solid), // Asegúrate de tener un ícono de edificio en tus recursos
                contentDescription = null,
                colorFilter = tint(Color.White),
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Name: ${building.name}", color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text("Latitude: ${building.lat}", color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text("Longitude: ${building.lng}", color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Preview
@Composable
fun BuildingListScreenPreview() {
    Surface(color = Color.White) {
        BuildingListScreen(rememberNavController())
    }
}