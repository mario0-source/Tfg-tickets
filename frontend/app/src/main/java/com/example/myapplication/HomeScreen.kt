package com.example.myapplication


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun HomeDashboardScreen(navController: NavHostController) {

    val primaryGreen = Color(0xFF00FF85)

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 🔹 Imagen de fondo
            Image(
                painter = painterResource(id = R.drawable.dashborad_fondo),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(20.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.perfil),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.imagen_login),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("GASTO MENSUAL", color = Color(0xFFB0B0C0), fontSize = 13.sp)

                Text(
                    text = "126.80€",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "+12%",
                    color = primaryGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(title = "TICKETS", value = "24")
                    StatCard(title = "CATEGORÍAS", value = "8")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Añadir Ticket",
                        color = Color(0xFF020208),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoCard(title = "PERSPECTIVAS", subtitle = "Ahorro Inteligente")
                    InfoCard(title = "PRÓXIMOS", subtitle = "3 Suscripciones")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Tickets Recientes",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                TicketItem("Starbucks Reserve", "5.40€", "COMIDA", "Hoy, 08:42 AM")
                TicketItem("Whole Foods Market", "42.15€", "DESPENSA", "Ayer, 06:15 PM")
                TicketItem("Nike Flagship", "79.25€", "MODA", "24 Oct, 02:30 PM")
            }
        }
    }
}


@Composable
fun StatCard(title: String, value: String) {
    Box(
        modifier = Modifier
            .height(90.dp)
            .background(Color(0xFF14141A), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(title, color = Color(0xFFB0B0C0), fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoCard(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .height(90.dp)
            .background(Color(0xFF14141A), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(title, color = Color(0xFFB0B0C0), fontSize = 12.sp)
            Text(subtitle, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun TicketItem(name: String, price: String, category: String, time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(category, color = Color(0xFFB0B0C0), fontSize = 12.sp)
            Text(time, color = Color(0xFF777777), fontSize = 11.sp)
        }
        Text(price, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {

    val currentRoute = navController.currentBackStackEntryFlow
        .collectAsState(initial = null).value?.destination?.route

    NavigationBar(containerColor = Color(0xFF0D0D12)) {

        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { navController.navigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Inicio") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color(0xFF777777),
                selectedTextColor = Color.White,
                unselectedTextColor = Color(0xFF777777),
                indicatorColor = Color(0xFF00FF85)
            )
        )

        NavigationBarItem(
            selected = currentRoute == "tickets",
            onClick = { navController.navigate("tickets") },
            icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
            label = { Text("Tickets") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color(0xFF777777),
                selectedTextColor = Color.White,
                unselectedTextColor = Color(0xFF777777),
                indicatorColor = Color(0xFF00FF85)
            )
        )

        NavigationBarItem(
            selected = currentRoute == "add",
            onClick = { navController.navigate("add") },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            label = { Text("Añadir") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color(0xFF777777),
                selectedTextColor = Color.White,
                unselectedTextColor = Color(0xFF777777),
                indicatorColor = Color(0xFF00FF85)
            )
        )

        NavigationBarItem(
            selected = currentRoute == "compare",
            onClick = { navController.navigate("compare") },
            icon = { Icon(Icons.Default.Search, contentDescription = null) },
            label = { Text("Comparar") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color(0xFF777777),
                selectedTextColor = Color.White,
                unselectedTextColor = Color(0xFF777777),
                indicatorColor = Color(0xFF00FF85)
            )
        )

        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { navController.navigate("profile") },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Perfil") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color(0xFF777777),
                selectedTextColor = Color.White,
                unselectedTextColor = Color(0xFF777777),
                indicatorColor = Color(0xFF00FF85)
            )
        )
    }
}


