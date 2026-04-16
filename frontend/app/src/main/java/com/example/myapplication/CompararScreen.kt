package com.example.myapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun DashboardScreen(navController: NavHostController) {

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            // Fondo
            Image(
                painter = painterResource(id = R.drawable.imagen_login),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Capa oscura
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )

            // CONTENIDO
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {

                PeriodSelector()

                Spacer(Modifier.height(20.dp))

                TotalGastoCardDark()

                Spacer(Modifier.height(25.dp))

                Text(
                    "Gastos por Tienda",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )

                val tiendas = listOf(
                    "Mercadona" to "420,00€",
                    "Carrefour" to "310,00€",
                    "IKEA" to "280,45€",
                    "Amazon" to "230,00€"
                )

                tiendas.forEach {
                    TiendaRowDark(nombre = it.first, cantidad = it.second)
                }

                Spacer(Modifier.height(25.dp))

                Text(
                    "Distribución por Categoría",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )

                val categorias = listOf(
                    "Comida" to 0.45f,
                    "Limpieza" to 0.25f,
                    "Electrónica" to 0.20f,
                    "Otros" to 0.10f
                )

                PieChartDark(categorias)

                Spacer(Modifier.height(25.dp))

                AnalisisInteligenteDark()

                Spacer(Modifier.height(100.dp))
            }
        }
    }
}
@Composable
fun PeriodSelector() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("Semana", "Mes", "Perso", "Tiendas").forEach {
            Text(
                text = it,
                fontWeight = if (it == "Semana") FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun TotalGastoCardDark() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF14141A).copy(alpha = 0.85f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Gasto Total", fontSize = 18.sp, color = Color(0xFFB0B0C0))
            Text("1.240,45€", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("+12% vs mes anterior", color = Color(0xFF00FF85))
        }
    }
}

@Composable
fun TiendaRowDark(nombre: String, cantidad: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(nombre, fontSize = 18.sp, color = Color.White)
        Text(cantidad, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun PieChartDark(data: List<Pair<String, Float>>) {
    val colors = listOf(
        Color(0xFF00FF85),
        Color(0xFF1A8CFF),
        Color(0xFFFF9800),
        Color(0xFFB84DFF)
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp)
        ) {
            var startAngle = -90f
            data.forEachIndexed { index, entry ->
                val sweep = entry.second * 360f
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    size = Size(size.width, size.height)
                )
                startAngle += sweep
            }
        }
    }
}

@Composable
fun AnalisisInteligenteDark() {
    Column {
        Text("Análisis Inteligente", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)

        Spacer(Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14141A).copy(alpha = 0.85f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Pico de Gasto", fontWeight = FontWeight.Bold, color = Color.White)
                Text("Has gastado un 20% más que el mes pasado, principalmente en Electrónica.", color = Color(0xFFB0B0C0))
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14141A).copy(alpha = 0.85f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Tienda Favorita", fontWeight = FontWeight.Bold, color = Color.White)
                Text("Tu tienda con mayor gasto es Mercadona. Has visitado 6 veces esta semana.", color = Color(0xFFB0B0C0))
            }
        }
    }
}


