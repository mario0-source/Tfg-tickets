package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun ProfileScreen(navController: NavHostController) {

    val primaryGreen = Color(0xFF00FF85)

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            // 🔹 Fondo tipo Nebula
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF0A0A0F),
                                Color(0xFF0D1B2A),
                                Color(0xFF003C3C)
                            )
                        )
                    )
            )

            // 🔹 Capa oscura
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )

            // 🔹 Contenido principal
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 🔹 Logo
                Icon(
                    painter = painterResource(id = R.drawable.imagen_login),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Digital Nebula",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 Foto de perfil
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.perfil),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "María López",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "MIEMBRO PREMIUM • DESDE 2023",
                    color = Color(0xFFB0B0C0),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 Estadísticas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileStatCard("AHORRO TOTAL", "$12,450.00")
                    ProfileStatCard("TICKETS ACTIVOS", "24")
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 🔹 Opciones del perfil
                ProfileOption("Editar Perfil", R.drawable.perfil)
                ProfileOption("Cambiar Contraseña", R.drawable.perfil)
                ProfileOption("Gestionar Categorías", R.drawable.perfil)
                ProfileOption("Administración", R.drawable.perfil)

                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 Botón cerrar sesión
                Button(
                    onClick = { /* logout */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D4D)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Cerrar Sesión",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "VERSIÓN APP 2.6.0-NEBULA",
                    color = Color(0xFF777777),
                    fontSize = 12.sp
                )
            }
        }
    }
}
@Composable
fun ProfileStatCard(title: String, value: String) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(title, color = Color(0xFFB0B0C0), fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
@Composable
fun ProfileOption(text: String, iconRes: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, color = Color.White, fontSize = 16.sp)
    }
}


