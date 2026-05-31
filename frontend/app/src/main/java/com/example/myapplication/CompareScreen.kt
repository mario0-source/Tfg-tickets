package com.example.myapplication

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.model.CompareProductSummary
import com.example.myapplication.ui.compare.CompareViewModel

@Composable
fun CompareScreen(navController: NavHostController) {

    val primaryGreen = Color(0xFF00FF85)
    val viewModel: CompareViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    var search by remember { mutableStateOf("") }

    val filtered = state.products.filter {
        it.productName.contains(search, ignoreCase = true)
    }

    LaunchedEffect(Unit) { viewModel.loadProducts() }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.imagen_login),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))

            if (state.loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = primaryGreen)
            }

            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text("Comparar Precios", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Selecciona un producto para comparar entre tiendas", color = Color(0xFFB0B0C0), fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = { Text("Buscar producto...", color = Color(0xFF9A9A9A)) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        )
                    )

                    Spacer(Modifier.height(16.dp))
                    CompareSummaryCard(state.products.size, state.ticketsWithProducts)
                    state.error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = Color(0xFFFF8080), fontSize = 13.sp)
                    }
                }

                if (filtered.isEmpty() && !state.loading) {
                    item {
                        Card(
                            Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF14141A).copy(alpha = 0.85f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                "Escanea tickets con productos para comparar precios.",
                                color = Color(0xFFB0B0C0),
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    }
                }

                itemsIndexed(filtered) { index, product ->
                    AnimatedVisibility(true, enter = fadeIn(tween(250, delayMillis = index * 40))) {
                        CompareProductListCard(
                            product = product,
                            accent = primaryGreen,
                            onClick = {
                                val encoded = Uri.encode(product.productName)
                                navController.navigate("compareDetail/$encoded")
                            }
                        )
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun CompareProductListCard(
    product: CompareProductSummary,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14141A).copy(alpha = 0.92f))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.ShoppingCart, null, tint = accent, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(product.productName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "${product.storeCount} tiendas · desde %.2f€".format(product.minPrice),
                        color = Color(0xFFB0B0C0),
                        fontSize = 12.sp
                    )
                    if (product.priceDiff > 0) {
                        Text("-%.2f€ de ahorro posible".format(product.priceDiff), color = accent, fontSize = 11.sp)
                    }
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFB0B0C0))
        }
    }
}

@Composable
fun CompareSummaryCard(productsCount: Int, ticketsCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14141A).copy(alpha = 0.85f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF1A3FFF).copy(alpha = 0.25f),
                            Color(0xFF00FF85).copy(alpha = 0.12f)
                        )
                    )
                )
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CompareStatItem("Productos", productsCount.toString())
            CompareStatItem("Tickets", ticketsCount.toString())
        }
    }
}

@Composable
fun CompareStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color(0xFFB0B0C0), fontSize = 12.sp)
    }
}
