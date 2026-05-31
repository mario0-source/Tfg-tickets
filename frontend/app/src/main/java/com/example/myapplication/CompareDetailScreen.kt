package com.example.myapplication

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.model.ComparePriceEntry
import com.example.myapplication.ui.compare.CompareDetailViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun CompareDetailScreen(
    navController: NavHostController,
    encodedProductName: String,
    viewModel: CompareDetailViewModel = viewModel()
) {
    val primaryGreen = Color(0xFF00FF85)
    val productName = remember(encodedProductName) {
        URLDecoder.decode(encodedProductName, StandardCharsets.UTF_8.toString())
    }
    val state by viewModel.state.collectAsState()

    var manualStore by remember { mutableStateOf("") }
    var manualPrice by remember { mutableStateOf("") }

    LaunchedEffect(productName) { viewModel.loadProduct(productName) }

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
            state.detail == null -> {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.error ?: "Producto no encontrado", color = Color.White)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) { Text("Volver") }
                }
            }
            else -> {
                val detail = state.detail!!
                val cheapest = detail.entries.minByOrNull { it.price }

                LazyColumn(
                    Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                        Text(productName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }

                    item {
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f))
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                CompareMetric("Mínimo", "%.2f€".format(detail.minPrice), primaryGreen)
                                CompareMetric("Máximo", "%.2f€".format(detail.maxPrice), Color(0xFFFF8080))
                                CompareMetric("Diferencia", "%.2f€".format(detail.priceDiff), Color(0xFF1A8CFF))
                            }
                        }
                    }

                    item {
                        Text("Evolución de precios", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        PriceBarChart(detail.entries, primaryGreen)
                    }

                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            detail.entries.map { it.store }.distinct().take(4).forEach { store ->
                                AssistChip(
                                    onClick = {},
                                    label = { Text(store, fontSize = 11.sp) },
                                    leadingIcon = { Icon(Icons.Default.Store, null, Modifier.size(14.dp)) }
                                )
                            }
                        }
                    }

                    items(detail.entries) { entry ->
                        CompareDetailEntryRow(entry, entry.store == cheapest?.store, primaryGreen)
                    }

                    item {
                        Text("Añadir precio manual", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = manualStore,
                            onValueChange = { manualStore = it },
                            label = { Text("Tienda") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = compareFieldColors()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = manualPrice,
                            onValueChange = { manualPrice = it },
                            label = { Text("Precio") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = compareFieldColors()
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val price = manualPrice.replace(",", ".").toDoubleOrNull() ?: return@Button
                                if (manualStore.isBlank()) return@Button
                                viewModel.addManualEntry(productName, manualStore.trim(), price)
                                manualStore = ""
                                manualPrice = ""
                            },
                            enabled = !state.saving,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.Black)
                            Spacer(Modifier.width(6.dp))
                            Text("Añadir", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color(0xFFB0B0C0), fontSize = 11.sp)
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
private fun CompareDetailEntryRow(entry: ComparePriceEntry, isCheapest: Boolean, accent: Color) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCheapest) accent.copy(alpha = 0.12f) else Color(0xFF14141A).copy(alpha = 0.85f)
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(entry.store, color = if (isCheapest) accent else Color.White, fontWeight = FontWeight.SemiBold)
                Text(
                    "${entry.date ?: "—"} · ${if (entry.source == "manual") "Manual" else "Ticket"}",
                    color = Color(0xFFB0B0C0),
                    fontSize = 11.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isCheapest) {
                    Icon(Icons.AutoMirrored.Filled.TrendingDown, null, tint = accent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                }
                Text("%.2f€".format(entry.price), color = if (isCheapest) accent else Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PriceBarChart(entries: List<ComparePriceEntry>, accent: Color) {
    if (entries.isEmpty()) return

    val maxPrice = entries.maxOf { it.price }.coerceAtLeast(0.01)
    val barColors = listOf(accent, Color(0xFF1A8CFF), Color(0xFFB84DFF), Color(0xFFFFB020))

    Card(
        Modifier.fillMaxWidth().height(180.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14141A).copy(alpha = 0.85f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Canvas(Modifier.fillMaxSize().padding(16.dp)) {
            val barWidth = size.width / (entries.size * 1.6f)
            val spacing = barWidth * 0.6f

            entries.forEachIndexed { index, entry ->
                val barHeight = (entry.price / maxPrice * (size.height - 20f)).toFloat()
                val x = index * (barWidth + spacing)
                val y = size.height - barHeight

                drawRoundRect(
                    color = barColors[index % barColors.size],
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }
        }
    }
}

@Composable
private fun compareFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFF00FF85),
    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
)
