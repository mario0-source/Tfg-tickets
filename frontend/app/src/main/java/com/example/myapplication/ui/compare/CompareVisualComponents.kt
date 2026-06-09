package com.example.myapplication.ui.compare

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.ComparePriceEntry
import com.example.myapplication.ui.theme.NebulaGreen
import com.example.myapplication.ui.theme.NebulaTextSecondary

@Composable
fun StoreColorDot(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun CompareStoreLegend(
    storeColors: Map<String, Color>,
    modifier: Modifier = Modifier
) {
    if (storeColors.isEmpty()) return

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Leyenda · tiendas",
            color = NebulaTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        FlowLegendRow(storeColors)
    }
}

@Composable
private fun FlowLegendRow(storeColors: Map<String, Color>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        storeColors.entries.chunked(2).forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { (store, color) ->
                    Row(
                        Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StoreColorDot(color)
                        Text(
                            store,
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ComparePriceBarChart(
    entries: List<ComparePriceEntry>,
    storeColors: Map<String, Color>,
    cheapestStore: String?,
    animateBars: Boolean,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) return

    val maxPrice = entries.maxOf { it.price }.coerceAtLeast(0.01)

    Card(
        modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14141A).copy(alpha = 0.85f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            var barsReady by remember(entries, animateBars) { mutableStateOf(!animateBars) }
            LaunchedEffect(entries, animateBars) {
                if (animateBars) {
                    barsReady = false
                    delay(40)
                    barsReady = true
                } else {
                    barsReady = true
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(112.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                entries.forEach { entry ->
                    val color = storeColors[entry.store] ?: CompareStoreColors.colorForStore(entry.store)
                    val targetFraction = (entry.price / maxPrice).toFloat().coerceIn(0.12f, 1f)
                    val fraction by animateFloatAsState(
                        targetValue = if (barsReady) targetFraction else 0.05f,
                        animationSpec = tween(480),
                        label = "bar_${entry.store}"
                    )
                    val isCheapest = entry.store == cheapestStore

                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "%.2f€".format(entry.price),
                            color = if (isCheapest) NebulaGreen else color,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            Modifier
                                .fillMaxWidth(0.72f)
                                .height(88.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(fraction)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                color.copy(alpha = if (isCheapest) 1f else 0.85f),
                                                color.copy(alpha = 0.35f)
                                            )
                                        )
                                    )
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            entry.store.take(10),
                            color = NebulaTextSecondary,
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            CompareStoreLegend(storeColors)
        }
    }
}

@Composable
fun ComparePriceRangeStrip(
    minPrice: Double,
    maxPrice: Double,
    modifier: Modifier = Modifier
) {
    if (maxPrice <= minPrice) return

    Column(modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(NebulaGreen.copy(alpha = 0.85f), Color(0xFFFF8080).copy(alpha = 0.65f))
                    )
                )
        )
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("mín %.2f€".format(minPrice), color = NebulaGreen, fontSize = 10.sp)
            Text("máx %.2f€".format(maxPrice), color = Color(0xFFFF9999), fontSize = 10.sp)
        }
    }
}
