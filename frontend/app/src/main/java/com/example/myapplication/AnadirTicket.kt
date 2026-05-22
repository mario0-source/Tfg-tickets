package com.example.myapplication

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.model.CreateTicketRequest
import com.example.myapplication.network.RetrofitClient
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun AddTicketScreen(navController: NavHostController) {

    val primaryGreen = Color(0xFF00FF85)

    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }

    // 🆕 PRODUCTOS DINÁMICOS
    var productos by remember { mutableStateOf(listOf("")) }

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->

        if (uri != null) {

            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->

                    val text = visionText.text

                    // -------------------------
                    // PRECIO
                    // -------------------------
                    val priceRegex = Regex("""(\d+[.,]\d{1,2})""")

                    val candidates = priceRegex.findAll(text)
                        .map { it.value.replace(",", ".").toDoubleOrNull() }
                        .filterNotNull()
                        .toList()

                    precio = candidates.maxOrNull()?.toString() ?: ""

                    // -------------------------
                    // TIENDA
                    // -------------------------
                    val lines = text.lines()

                    val stopWords = listOf(
                        "ticket", "factura", "iva", "total", "cambio", "eur", "€"
                    )

                    nombre = lines.firstOrNull { line ->
                        val clean = line.trim().lowercase()
                        clean.length > 3 &&
                                stopWords.none { it in clean } &&
                                clean.any { it.isLetter() }
                    } ?: ""

                    // -------------------------
                    // CATEGORÍA
                    // -------------------------
                    val lower = text.lowercase()

                    categoria = when {
                        listOf("mercadona", "carrefour", "lidl").any { it in lower } ->
                            "Supermercado"

                        listOf("apple", "fnac", "media markt").any { it in lower } ->
                            "Tecnología"

                        else -> "General"
                    }

                    // -------------------------
                    // PRODUCTOS (básico OCR split)
                    // -------------------------
                    val forbiddenWords = listOf(
                        "total",
                        "iva",
                        "fecha",
                        "vendedor",
                        "caja",
                        "hora",
                        "eur",
                        "factura",
                        "simplificada",
                        "artículos",
                        "numero",
                        "tarjeta",
                        "cambio"
                    )

                    val productRegex = Regex("""([A-Za-zÁÉÍÓÚáéíóúñÑ\s]+)\s+(\d+[.,]\d{2})""")

                    productos = lines.mapNotNull { line ->

                        val match = productRegex.find(line)

                        if (match != null) {

                            val productName = match.groupValues[1]
                                .trim()

                            val lower = productName.lowercase()

                            val forbidden = listOf(
                                "total",
                                "iva",
                                "fecha",
                                "vendedor",
                                "caja",
                                "hora",
                                "eur",
                                "factura",
                                "simplificada"
                            )

                            if (
                                productName.length > 2 &&
                                forbidden.none { it in lower }
                            ) {
                                productName
                            } else {
                                null
                            }

                        } else {
                            null
                        }

                    }.distinct()
                }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

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

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                Text("Añadir Ticket", color = Color.White, fontSize = 24.sp)

                // -------------------------
                // PRECIO
                // -------------------------
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio") },
                    modifier = Modifier.fillMaxWidth()
                )

                // -------------------------
                // TIENDA
                // -------------------------
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Tienda") },
                    modifier = Modifier.fillMaxWidth()
                )

                // -------------------------
                // FECHA (corrigiendo bug)
                // -------------------------
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("Fecha") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth()
                )

                // -------------------------
                // 🆕 PRODUCTOS DINÁMICOS
                // -------------------------
                Text("Productos", color = Color.White)

                productos.forEachIndexed { index, item ->

                    OutlinedTextField(
                        value = item,
                        onValueChange = { newValue ->
                            productos = productos.toMutableList().also {
                                it[index] = newValue
                            }
                        },
                        label = { Text("Producto ${index + 1}") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = {
                        productos = productos + ""
                    },
                    colors = ButtonDefaults.buttonColors(primaryGreen)
                ) {
                    Text("+ Añadir producto", color = Color.Black)
                }

                // -------------------------
                // OCR BOTÓN
                // -------------------------
                Button(
                    onClick = { launcher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(primaryGreen)
                ) {
                    Text("Seleccionar Ticket", color = Color.Black)
                }

                // -------------------------
                // GUARDAR
                // -------------------------
                Button(
                    onClick = {

                        val request = CreateTicketRequest(
                            nombre = nombre,
                            precio = precio.toDoubleOrNull() ?: 0.0,
                            categoria = categoria,
                            productos = productos.filter { it.isNotBlank() }
                        )

                        RetrofitClient.api.createTicket(request)
                            .enqueue(object : Callback<Map<String, Any>> {
                                override fun onResponse(
                                    call: Call<Map<String, Any>>,
                                    response: Response<Map<String, Any>>
                                ) {
                                    if (response.isSuccessful) {
                                        navController.navigate("home")
                                    }
                                }

                                override fun onFailure(
                                    call: Call<Map<String, Any>>,
                                    t: Throwable
                                ) {}
                            })
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(primaryGreen)
                ) {
                    Text("Guardar Ticket", color = Color.Black)
                }
            }
        }
    }
}