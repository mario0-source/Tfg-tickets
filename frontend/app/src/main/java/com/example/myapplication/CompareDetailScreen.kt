package com.example.myapplication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.model.ComparePriceEntry
import com.example.myapplication.ui.compare.CompareDetailViewModel
import com.example.myapplication.ui.compare.ComparePriceBarChart
import com.example.myapplication.ui.compare.CompareStoreColors
import com.example.myapplication.ui.compare.StoreColorDot
import com.example.myapplication.ui.components.ValidatedOutlinedField
import com.example.myapplication.ui.theme.NebulaGreen
import com.example.myapplication.ui.theme.NebulaScreenBackground
import com.example.myapplication.ui.theme.NebulaTextSecondary
import com.example.myapplication.util.FormValidators
import kotlinx.coroutines.delay
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareDetailScreen(
    navController: NavHostController,
    encodedProductName: String,
    viewModel: CompareDetailViewModel = viewModel()
) {
    val primaryGreen = NebulaGreen
    val productName = remember(encodedProductName) {
        URLDecoder.decode(encodedProductName, StandardCharsets.UTF_8.toString())
    }
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var manualStore by remember { mutableStateOf("") }
    var manualPrice by remember { mutableStateOf("") }
    var submitAttempted by remember { mutableStateOf(false) }

    val storeError = remember(manualStore, submitAttempted) {
        if (!submitAttempted && manualStore.isBlank()) null else FormValidators.validateRequired(manualStore, "La tienda")
    }
    val priceError = remember(manualPrice, submitAttempted) {
        if (!submitAttempted && manualPrice.isBlank()) null else FormValidators.validatePrice(manualPrice)
    }

    LaunchedEffect(productName) { viewModel.loadProduct(productName) }

    LaunchedEffect(state.saveMessage) {
        state.saveMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            delay(2800)
            viewModel.clearTransientFeedback()
        }
    }

    NebulaScreenBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color(0xFF1A2E24),
                        contentColor = primaryGreen,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        ) { padding ->
            when {
                state.loading -> {
                    CircularProgressIndicator(
                        Modifier
                            .padding(padding)
                            .align(Alignment.Center),
                        color = primaryGreen
                    )
                }
                state.detail == null -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(24.dp),
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
                    val sortedEntries = remember(detail.entries) { detail.entries.sortedBy { it.price } }
                    val cheapest = sortedEntries.firstOrNull()
                    val storeColors = remember(sortedEntries) {
                        CompareStoreColors.colorsByStore(sortedEntries.map { it.store })
                    }

                    LazyColumn(
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                            }
                            Text(
                                productName,
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            if (cheapest != null) {
                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = primaryGreen.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.TrendingDown,
                                            null,
                                            tint = primaryGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            "Mejor precio: ${cheapest.store} · %.2f€".format(cheapest.price),
                                            color = primaryGreen,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
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

                        if (sortedEntries.size >= 2) {
                            item {
                                Text(
                                    "Precios por tienda",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(Modifier.height(10.dp))
                                ComparePriceBarChart(
                                    entries = sortedEntries,
                                    storeColors = storeColors,
                                    cheapestStore = cheapest?.store,
                                    animateBars = !state.saving
                                )
                            }
                        }

                        item {
                            Text(
                                "Detalle por tienda",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        itemsIndexed(sortedEntries) { index, entry ->
                            val isHighlighted = entry.store.equals(state.highlightStore, ignoreCase = true)
                            val highlightScale by animateFloatAsState(
                                targetValue = if (isHighlighted) 1.02f else 1f,
                                animationSpec = tween(300),
                                label = "rowScale"
                            )
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(280)) + scaleIn(
                                    initialScale = 0.92f,
                                    animationSpec = tween(280)
                                )
                            ) {
                                Box(Modifier.graphicsLayerScale(highlightScale)) {
                                    CompareDetailEntryRow(
                                        entry = entry,
                                        isCheapest = index == 0,
                                        storeColor = storeColors[entry.store] ?: primaryGreen,
                                        highlighted = isHighlighted
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        "¿Lo viste más barato en otra tienda?",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "Anota tienda y precio: aparecerá en el gráfico con su color.",
                                        color = NebulaTextSecondary,
                                        style = MaterialTheme.typography.bodySmall,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(Modifier.height(14.dp))
                                    ValidatedOutlinedField(
                                        value = manualStore,
                                        onValueChange = { manualStore = it },
                                        label = "Tienda donde lo viste",
                                        error = storeError,
                                        placeholder = "Ej: Dia, Lidl, Mercadona…"
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    ValidatedOutlinedField(
                                        value = manualPrice,
                                        onValueChange = { manualPrice = it },
                                        label = "Precio que viste (€)",
                                        error = priceError,
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                                        placeholder = "Ej: 1,15"
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            submitAttempted = true
                                            if (storeError != null || priceError != null) return@Button

                                            val price = manualPrice.replace(",", ".").toDoubleOrNull() ?: return@Button
                                            viewModel.addManualEntry(productName, manualStore.trim(), price)
                                            manualStore = ""
                                            manualPrice = ""
                                            submitAttempted = false
                                        },
                                        enabled = !state.saving,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                                    ) {
                                        if (state.saving) {
                                            CircularProgressIndicator(
                                                Modifier.size(20.dp),
                                                color = Color.Black,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(Icons.Default.Add, null, tint = Color.Black)
                                            Spacer(Modifier.width(6.dp))
                                            Text("Añadir al gráfico", color = Color.Black)
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
}

private fun Modifier.graphicsLayerScale(scale: Float): Modifier = graphicsLayer {
    scaleX = scale
    scaleY = scale
}

@Composable
private fun CompareMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = NebulaTextSecondary, fontSize = 11.sp)
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
private fun CompareDetailEntryRow(
    entry: ComparePriceEntry,
    isCheapest: Boolean,
    storeColor: Color,
    highlighted: Boolean
) {
    val borderColor = when {
        highlighted -> storeColor
        isCheapest -> NebulaGreen
        else -> Color.Transparent
    }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                highlighted -> storeColor.copy(alpha = 0.18f)
                isCheapest -> NebulaGreen.copy(alpha = 0.12f)
                else -> Color(0xFF14141A).copy(alpha = 0.85f)
            }
        ),
        border = if (borderColor != Color.Transparent) {
            androidx.compose.foundation.BorderStroke(1.5.dp, borderColor.copy(alpha = 0.7f))
        } else null
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier
                    .width(5.dp)
                    .height(40.dp)
                    .background(storeColor, RoundedCornerShape(4.dp))
            )
            Column(Modifier.weight(1f)) {
                Text(
                    entry.store,
                    color = if (isCheapest) NebulaGreen else Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${entry.date ?: "—"} · ${if (entry.source == "manual") "Anotado a mano" else "De un ticket"}",
                    color = NebulaTextSecondary,
                    fontSize = 11.sp
                )
            }
            Text(
                "%.2f€".format(entry.price),
                color = if (isCheapest) NebulaGreen else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        }
    }
}
