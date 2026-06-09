package com.example.myapplication


import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.auth.SessionManager
import com.example.myapplication.ui.components.NebulaProfileAvatar
import com.example.myapplication.ui.home.HomeViewModel
import com.example.myapplication.ui.theme.NebulaGreen
import com.example.myapplication.ui.theme.NebulaScreenBackground
import com.example.myapplication.ui.theme.NebulaTextSecondary

@Composable
fun HomeDashboardScreen(navController: NavHostController) {

    val primaryGreen = NebulaGreen
    val viewModel: HomeViewModel = viewModel()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userEmail = remember { sessionManager.getEmail() }
    val userName = remember(userEmail) {
        userEmail?.substringBefore("@")?.replaceFirstChar { it.uppercaseChar() } ?: "Usuario"
    }

    val state by viewModel.state.collectAsState()
    val variationLabel = when {
        state.variation > 0 -> "+${state.variation.toInt()}%"
        state.variation < 0 -> "${state.variation.toInt()}%"
        else -> "0%"
    }
    val variationColor = if (state.variation >= 0) primaryGreen else Color(0xFFFF8080)
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->

        NebulaScreenBackground(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.clickable { navController.navigate("profile") }
                        ) {
                            NebulaProfileAvatar(
                                email = userEmail,
                                onClick = { navController.navigate("profile") }
                            )
                            Column {
                                Text(
                                    text = "Hola, $userName",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Tu espacio Nebula",
                                    color = NebulaTextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            tint = primaryGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("GASTO MENSUAL", color = NebulaTextSecondary, style = MaterialTheme.typography.labelMedium)

                    Text(
                        text = "${state.total}€",
                        color = Color.White,
                        style = MaterialTheme.typography.displayLarge
                    )

                    Text(
                        text = variationLabel,
                        color = variationColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(title = "TICKETS", value = state.ticketsCount.toString())
                        StatCard(title = "CATEGORÍAS", value = state.categoriesCount.toString())
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { navController.navigate("add") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF020208),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Añadir Ticket",
                            color = Color(0xFF020208),
                            style = MaterialTheme.typography.labelLarge
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
                }

                items(state.tickets.take(5)) { ticket ->
                    TicketItem(
                        name = ticket.nombre,
                        price = "${ticket.precio}€",
                        category = ticket.categoria,
                        time = ticket.fecha,
                        onClick = { navController.navigate("ticketDetail/${ticket.id}") }
                    )
                }
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
fun TicketItem(
    name: String,
    price: String,
    category: String,
    time: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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


