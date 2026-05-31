package com.example.myapplication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.auth.SessionManager
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.ui.profile.ProfileViewModel

@Composable
fun ProfileScreen(navController: NavHostController) {
    val primaryGreen = Color(0xFF00FF85)
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val viewModel: ProfileViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) { viewModel.loadProfile(sessionManager.getEmail()) }

    val profile = state.profile
    val email = profile?.email ?: sessionManager.getEmail() ?: "—"
    val ticketsCount = profile?.ticketsCount?.toString() ?: "0"
    val totalSpent = profile?.let { "%.2f€".format(it.totalSpent) } ?: "0.00€"
    val avgSpend = profile?.let { "%.2f€".format(it.avgSpendPerTicket) } ?: "0.00€"
    val monthlySpent = profile?.let { "%.2f€".format(it.monthlySpent) } ?: "0.00€"
    val topCategory = profile?.topCategory ?: "General"

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0A0A0F), Color(0xFF0D1B2A), Color(0xFF003C3C))
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )

            if (state.loading && profile == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = primaryGreen
                )
            }

            AnimatedVisibility(
                visible = profile != null || !state.loading,
                enter = fadeIn(tween(400))
            ) {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Mi Perfil", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF1A3FFF).copy(alpha = 0.4f),
                                        Color(0xFF00FF85).copy(alpha = 0.25f)
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painterResource(R.drawable.perfil),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, null, tint = primaryGreen, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(email, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileStatCard("GASTO TOTAL", totalSpent, Icons.Default.AccountBalanceWallet, primaryGreen, Modifier.weight(1f))
                        ProfileStatCard("TICKETS", ticketsCount, Icons.Default.Receipt, Color(0xFF1A8CFF), Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileStatCard("MEDIA/TICKET", avgSpend, Icons.Default.TrendingUp, Color(0xFFB84DFF), Modifier.weight(1f))
                        ProfileStatCard("ESTE MES", monthlySpent, Icons.Default.AccountBalanceWallet, Color(0xFFFFB020), Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(20.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFF1A3FFF).copy(alpha = 0.15f),
                                            Color(0xFF00FF85).copy(alpha = 0.08f)
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Text("Dashboard financiero", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Category, null, tint = primaryGreen, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Categoría más usada: $topCategory", color = Color(0xFFB0B0C0), fontSize = 14.sp)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Has registrado $ticketsCount tickets. Gasto acumulado: $totalSpent. Media por ticket: $avgSpend.",
                                color = Color(0xFFB0B0C0),
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    state.error?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = Color(0xFFFF8080), fontSize = 13.sp)
                    }

                    Spacer(Modifier.height(28.dp))

                    Button(
                        onClick = {
                            sessionManager.clearToken()
                            RetrofitClient.setToken(null)
                            navController.navigate("login") { popUpTo(0) }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D4D)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Cerrar Sesión", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, color = Color(0xFFB0B0C0), fontSize = 10.sp)
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
