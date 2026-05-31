package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.ui.tickets.TicketsViewModel

@Composable
fun TicketsScreen(navController: NavHostController) {

    val primaryGreen = Color(0xFF00FF85)

    val viewModel: TicketsViewModel = viewModel()

    val state by viewModel.state.collectAsState()

    var search by remember { mutableStateOf("") }

    val filteredTickets = state.tickets.filter {

        it.nombre.contains(search, ignoreCase = true) ||
                it.categoria.contains(search, ignoreCase = true)
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            Image(
                painter = painterResource(id = R.drawable.imagen_login),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )

            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),

                contentPadding = PaddingValues(vertical = 20.dp),

                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {

                    Text(
                        text = "Tickets Recientes",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },

                        placeholder = {
                            Text(
                                "Buscar tickets...",
                                color = Color(0xFF9A9A9A)
                            )
                        },

                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.White
                            )
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color.White.copy(alpha = 0.05f),
                                RoundedCornerShape(14.dp)
                            ),

                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = primaryGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFF14141A).copy(alpha = 0.85f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(20.dp)
                    ) {

                        Column {

                            Text(
                                "Gasto Mensual",
                                color = Color(0xFFB0B0C0),
                                fontSize = 13.sp
                            )

                            Text(
                                "%.2f€".format(state.total),
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "${state.ticketsCount} tickets registrados",
                                color = Color(0xFF777777),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF1A3FFF),
                                        Color(0xFF00C6FF)
                                    )
                                ),
                                RoundedCornerShape(18.dp)
                            )
                            .padding(20.dp)
                    ) {

                        Column {

                            Text(
                                "Exportar Smart",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "Descarga todos tus tickets en PDF.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { },

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White
                                ),

                                shape = RoundedCornerShape(12.dp)
                            ) {

                                Text(
                                    "Exportar Ahora",
                                    color = Color(0xFF0A0A0F),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Historial de Transacciones",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(filteredTickets) { ticket ->

                    TicketRow(
                        store = ticket.nombre,
                        price = "%.2f€".format(ticket.precio),
                        category = ticket.categoria,
                        date = ticket.fecha,
                        onClick = {
                            navController.navigate("ticketDetail/${ticket.id}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TicketRow(
    store: String,
    price: String,
    category: String,
    date: String,
    onClick: () -> Unit = {}
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column {

            Text(
                text = store,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = category,
                color = Color(0xFFB0B0C0),
                fontSize = 12.sp
            )

            Text(
                text = date,
                color = Color(0xFF777777),
                fontSize = 11.sp
            )
        }

        Text(
            text = price,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
