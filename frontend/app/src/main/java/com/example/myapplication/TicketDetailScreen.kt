package com.example.myapplication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.ui.tickets.TicketDetailViewModel
import com.example.myapplication.util.TicketPdfExporter
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    navController: NavHostController,
    ticketId: Int,
    viewModel: TicketDetailViewModel = viewModel()
) {
    val primaryGreen = Color(0xFF00FF85)
    val accentBlue = Color(0xFF1A8CFF)
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    LaunchedEffect(ticketId) { viewModel.loadTicket(ticketId) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0A0A0F), Color(0xFF0D1B2A), Color(0xFF003C3C))
                )
            )
    ) {
        when {
            state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = primaryGreen)
            state.ticket == null -> {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.error ?: "Ticket no encontrado", color = Color.White)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) { Text("Volver") }
                }
            }
            else -> {
                val ticket = state.ticket!!
                val productsSum = ticket.productos.sumOf { it.precio ?: 0.0 }

                LazyColumn(
                    Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    }

                    item {
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f))
                        ) {
                            Column(
                                Modifier.fillMaxWidth().background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFF1A3FFF).copy(alpha = 0.35f),
                                            Color(0xFF00FF85).copy(alpha = 0.15f)
                                        )
                                    )
                                ).padding(24.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Store, null, tint = primaryGreen, modifier = Modifier.size(28.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text(ticket.nombre, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Resumen financiero", color = Color(0xFFB0B0C0), fontSize = 13.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("%.2f€".format(ticket.precio), color = primaryGreen, fontSize = 38.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    SummaryMiniStat("Productos", ticket.productos.size.toString())
                                    SummaryMiniStat("Suma items", "%.2f€".format(productsSum))
                                }
                                Spacer(Modifier.height(14.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    DetailChip(Icons.Default.Category, ticket.categoria.ifBlank { "General" }, accentBlue)
                                    DetailChip(Icons.Default.CalendarMonth, formatTicketDate(ticket.fecha), Color(0xFFB84DFF))
                                }
                            }
                        }
                    }

                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    if (ticket.productos.isNotEmpty()) {
                                        val name = android.net.Uri.encode(ticket.productos.first().nombre)
                                        navController.navigate("compareDetail/$name")
                                    } else {
                                        navController.navigate("compare")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryGreen)
                            ) {
                                Icon(Icons.Default.CompareArrows, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Comparar", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    TicketPdfExporter.export(context, ticket)?.let {
                                        context.startActivity(android.content.Intent.createChooser(it, "Exportar PDF"))
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("PDF", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { navController.navigate("ticketEdit/$ticketId") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = accentBlue)
                            ) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Editar", fontSize = 12.sp)
                            }
                        }
                    }

                    item {
                        Text("Productos", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    if (ticket.productos.isEmpty()) {
                        item {
                            Card(
                                Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Sin productos registrados", color = Color(0xFFB0B0C0), modifier = Modifier.padding(20.dp))
                            }
                        }
                    } else {
                        itemsIndexed(ticket.productos) { index, producto ->
                            AnimatedVisibility(
                                true,
                                enter = fadeIn(tween(300, delayMillis = index * 60)) +
                                        slideInVertically(tween(300, delayMillis = index * 60)) { it / 2 }
                            ) {
                                Card(
                                    Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14141A).copy(alpha = 0.85f)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(18.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(Icons.Default.ShoppingBag, null, tint = primaryGreen, modifier = Modifier.size(22.dp))
                                            Spacer(Modifier.width(12.dp))
                                            Text(producto.nombre, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                        }
                                        Text(
                                            "%.2f€".format(producto.precio ?: 0.0),
                                            color = primaryGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMiniStat(label: String, value: String) {
    Column {
        Text(label, color = Color(0xFFB0B0C0), fontSize = 11.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
private fun DetailChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color
) {
    Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.18f)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private fun formatTicketDate(raw: String): String {
    val patterns = listOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy")
    for (pattern in patterns) {
        try {
            val input = SimpleDateFormat(pattern, Locale.getDefault())
            val output = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-ES"))
            val date = input.parse(raw.trim())
            if (date != null) return output.format(date)
        } catch (_: Exception) {}
    }
    return raw.ifBlank { "Sin fecha" }
}
