package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun TicketsScreen(navController: NavHostController) {

    val primaryGreen = Color(0xFF00FF85)
    val darkBg = Color(0xFF0A0A0F)

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
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

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(20.dp)
            ) {

                Text(
                    text = "Tickets Recientes",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                var search by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Buscar tiendas, artículos…", color = Color(0xFF9A9A9A)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp)),
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
                        .background(Color(0xFF14141A).copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Text("Gasto Mensual", color = Color(0xFFB0B0C0), fontSize = 13.sp)
                        Text("1.240,45€", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Text("+12%", color = primaryGreen, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("De 24 actividades recientes", color = Color(0xFF777777), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1A3FFF), Color(0xFF00C6FF))
                            ),
                            RoundedCornerShape(18.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text("Exportar Smart", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Descarga todos tus tickets en PDF para tu periodo fiscal.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Exportar Ahora", color = Color(0xFF0A0A0F), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Historial de Transacciones", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("VER TODO", color = primaryGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TicketRow("Mercadona", "42,80€", "SUPERMERCADO", "Hoy, 16:20")
                TicketRow("Carrefour", "129,00€", "ELECTRÓNICA", "Ayer, 18:45")
                TicketRow("IKEA", "314,20€", "MUEBLES", "22 Oct., 11:30")
                TicketRow("Apple Store", "29,99€", "SERVICIO", "19 Oct., 15:00")
            }
        }
    }
}



@Composable
fun TicketRow(store: String, price: String, category: String, date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(store, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(category, color = Color(0xFFB0B0C0), fontSize = 12.sp)
            Text(date, color = Color(0xFF777777), fontSize = 11.sp)
        }
        Text(price, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
