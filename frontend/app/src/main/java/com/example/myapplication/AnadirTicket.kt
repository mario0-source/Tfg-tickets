package com.example.myapplication

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
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
import androidx.navigation.NavHostController
import com.example.myapplication.model.CreateTicketRequest
import com.example.myapplication.model.ProductDto
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.ocr.ParsedReceipt
import com.example.myapplication.ocr.ReceiptOcrParser
import com.example.myapplication.ocr.TicketOcrHelper
import com.example.myapplication.util.TicketProductHelper
import com.example.myapplication.util.TicketTotalValidator
import com.example.myapplication.util.TicketValidationState
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun AddTicketScreen(navController: NavHostController) {

    val primaryGreen = Color(0xFF00FF85)
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var productos by remember { mutableStateOf(listOf(ProductDto("", null))) }

    val singleProductMode = productos.size == 1
    val totalValue = precio.replace(",", ".").toDoubleOrNull() ?: 0.0

    val validationState = remember(totalValue, productos, singleProductMode) {
        TicketTotalValidator.validate(totalValue, productos, singleProductMode)
    }

    val canSave = remember(validationState, totalValue, productos, singleProductMode) {
        when {
            nombre.isBlank() || totalValue <= 0 -> false
            productos.none { it.nombre.isNotBlank() } -> false
            singleProductMode -> true
            validationState is TicketValidationState.Valid -> true
            else -> false
        }
    }

    fun applyParsedReceipt(parsed: ParsedReceipt) {
        precio = parsed.precio
        nombre = parsed.nombre
        fecha = parsed.fecha
        categoria = parsed.categoria
        productos = if (parsed.productos.isNotEmpty()) {
            parsed.productos
        } else {
            listOf(ProductDto("", null))
        }

        if (parsed.precio.isBlank() && !parsed.hasValidProducts) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "OCR: no se detectó total ni productos. Complétalo manualmente."
                )
            }
        } else if (!parsed.hasValidProducts) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "OCR: total detectado. Revisa los productos."
                )
            }
        }
    }

    fun runOcr(uri: Uri) {
        TicketOcrHelper.processImage(
            context = context,
            uri = uri,
            onSuccess = { applyParsedReceipt(it) },
            onFailure = {
                scope.launch {
                    snackbarHostState.showSnackbar("No se pudo leer la imagen del ticket.")
                }
            }
        )
    }

    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { runOcr(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { runOcr(it) }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Añadir Ticket", color = Color.White, fontSize = 24.sp)

                ticketFields(
                    precio = precio,
                    onPrecioChange = { precio = it },
                    nombre = nombre,
                    onNombreChange = { nombre = it },
                    fecha = fecha,
                    onFechaChange = { fecha = it },
                    categoria = categoria,
                    onCategoriaChange = { categoria = it }
                )

                Text("Productos", color = Color.White)

                if (singleProductMode) {
                    Text(
                        "Un solo producto: usará el precio total automáticamente",
                        color = Color(0xFFB0B0C0),
                        fontSize = 12.sp
                    )
                }

                productListFields(
                    productos = productos,
                    onProductosChange = { productos = it },
                    singleProductMode = singleProductMode
                )

                ValidationBanner(
                    state = validationState,
                    onAutoAdjust = {
                        productos = TicketTotalValidator.autoAdjustProducts(productos, totalValue)
                    }
                )

                Button(
                    onClick = { productos = productos + ProductDto("", null) },
                    colors = ButtonDefaults.buttonColors(primaryGreen)
                ) {
                    Text("+ Añadir producto", color = Color.Black)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(primaryGreen)
                    ) {
                        Text("Galería", color = Color.Black)
                    }

                    Button(
                        onClick = {
                            val uri = TicketOcrHelper.createTempImageUri(context)
                            cameraUri = uri
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(primaryGreen)
                    ) {
                        Text("Cámara", color = Color.Black)
                    }
                }

                Button(
                    onClick = {
                        val prepared = TicketProductHelper.prepareForSave(productos, totalValue)
                        val request = CreateTicketRequest(
                            nombre = nombre,
                            precio = totalValue,
                            categoria = categoria,
                            productos = prepared
                        )
                        RetrofitClient.api.createTicket(request)
                            .enqueue(object : Callback<Map<String, Any>> {
                                override fun onResponse(
                                    call: Call<Map<String, Any>>,
                                    response: Response<Map<String, Any>>
                                ) {
                                    if (response.isSuccessful) navController.navigate("home")
                                }

                                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {}
                            })
                    },
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryGreen,
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    Text("Guardar Ticket", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ticketFields(
    precio: String,
    onPrecioChange: (String) -> Unit,
    nombre: String,
    onNombreChange: (String) -> Unit,
    fecha: String,
    onFechaChange: (String) -> Unit,
    categoria: String,
    onCategoriaChange: (String) -> Unit
) {
    OutlinedTextField(
        value = precio,
        onValueChange = onPrecioChange,
        label = { Text("Precio total") },
        modifier = Modifier.fillMaxWidth(),
        colors = outlinedFieldColors()
    )
    OutlinedTextField(
        value = nombre,
        onValueChange = onNombreChange,
        label = { Text("Tienda") },
        modifier = Modifier.fillMaxWidth(),
        colors = outlinedFieldColors()
    )
    OutlinedTextField(
        value = fecha,
        onValueChange = onFechaChange,
        label = { Text("Fecha") },
        modifier = Modifier.fillMaxWidth(),
        colors = outlinedFieldColors()
    )
    OutlinedTextField(
        value = categoria,
        onValueChange = onCategoriaChange,
        label = { Text("Categoría") },
        modifier = Modifier.fillMaxWidth(),
        colors = outlinedFieldColors()
    )
}

@Composable
fun productListFields(
    productos: List<ProductDto>,
    onProductosChange: (List<ProductDto>) -> Unit,
    singleProductMode: Boolean
) {
    productos.forEachIndexed { index, item ->
        if (singleProductMode) {
            OutlinedTextField(
                value = item.nombre,
                onValueChange = { newValue ->
                    onProductosChange(productos.toMutableList().also { it[index] = it[index].copy(nombre = newValue) })
                },
                label = { Text("Producto") },
                modifier = Modifier.fillMaxWidth(),
                colors = outlinedFieldColors()
            )
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = item.nombre,
                    onValueChange = { newValue ->
                        onProductosChange(productos.toMutableList().also { it[index] = it[index].copy(nombre = newValue) })
                    },
                    label = { Text("Producto ${index + 1}") },
                    modifier = Modifier.weight(1f),
                    colors = outlinedFieldColors()
                )
                OutlinedTextField(
                    value = item.precio?.toString()?.takeIf { it != "0.0" } ?: "",
                    onValueChange = { newValue ->
                        onProductosChange(
                            productos.toMutableList().also {
                                it[index] = it[index].copy(precio = newValue.replace(",", ".").toDoubleOrNull())
                            }
                        )
                    },
                    label = { Text("Precio") },
                    modifier = Modifier.width(100.dp),
                    colors = outlinedFieldColors()
                )
            }
        }
    }
}

@Composable
fun ValidationBanner(state: TicketValidationState, onAutoAdjust: () -> Unit) {
    when (state) {
        is TicketValidationState.Valid -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00FF85).copy(alpha = 0.12f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF00FF85))
                    Spacer(Modifier.width(10.dp))
                    Text("Total coincide con productos", color = Color.White, fontSize = 13.sp)
                }
            }
        }

        is TicketValidationState.SuggestAdjust -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFB020).copy(alpha = 0.15f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFFFB020))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Diferencia de %.2f€ (suma: %.2f€)".format(state.difference, state.productsSum),
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    TextButton(onClick = onAutoAdjust) {
                        Text("Aplicar ajuste automático", color = Color(0xFFFFB020), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        is TicketValidationState.Invalid -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFF4D4D).copy(alpha = 0.15f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, null, tint = Color(0xFFFF4D4D))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Total no coincide con productos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "Diferencia: %.2f€ | Suma: %.2f€ | Total: %.2f€".format(
                                state.difference, state.productsSum, state.total
                            ),
                            color = Color(0xFFB0B0C0),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        TicketValidationState.Idle -> Unit
    }
}

@Composable
fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFF00FF85),
    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
    focusedLabelColor = Color(0xFF00FF85),
    unfocusedLabelColor = Color(0xFFB0B0C0),
    cursorColor = Color(0xFF00FF85)
)
