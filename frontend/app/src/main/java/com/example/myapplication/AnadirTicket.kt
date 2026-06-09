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
import androidx.compose.material3.MaterialTheme
import com.example.myapplication.ui.components.ValidatedOutlinedField
import com.example.myapplication.ui.theme.NebulaGreen
import com.example.myapplication.ui.theme.NebulaScreenBackground
import com.example.myapplication.util.ApiErrorParser
import com.example.myapplication.util.FormValidators
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun AddTicketScreen(navController: NavHostController) {

    val primaryGreen = NebulaGreen
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var productos by remember { mutableStateOf(listOf(ProductDto("", null))) }
    var submitAttempted by remember { mutableStateOf(false) }

    val singleProductMode = productos.size == 1
    val totalValue = precio.replace(",", ".").toDoubleOrNull() ?: 0.0

    val nombreError = remember(nombre, submitAttempted) {
        if (!submitAttempted && nombre.isBlank()) null else FormValidators.validateRequired(nombre, "La tienda")
    }
    val precioError = remember(precio, submitAttempted) {
        if (!submitAttempted && precio.isBlank()) null else FormValidators.validatePrice(precio)
    }
    val fechaError = remember(fecha, submitAttempted) {
        FormValidators.validateDate(fecha, required = false)
    }
    val productErrors = remember(productos, singleProductMode, submitAttempted) {
        if (!submitAttempted) emptyMap() else FormValidators.validateProductPrices(productos, singleProductMode)
    }
    val productsMissingError = remember(productos, submitAttempted) {
        if (submitAttempted && !FormValidators.hasAnyProductName(productos)) {
            "Añade al menos un producto"
        } else null
    }

    val validationState = remember(totalValue, productos, singleProductMode) {
        TicketTotalValidator.validate(totalValue, productos, singleProductMode)
    }

    val canSave = remember(
        nombreError, precioError, fechaError, productErrors, productsMissingError,
        validationState, productos, singleProductMode
    ) {
        nombreError == null &&
                precioError == null &&
                fechaError == null &&
                productErrors.isEmpty() &&
                productsMissingError == null &&
                FormValidators.hasAnyProductName(productos) &&
                (singleProductMode || validationState is TicketValidationState.Valid)
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

        when {
            parsed.precio.isBlank() && !parsed.hasValidProducts -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        "OCR: no se detectó total ni productos. Complétalo manualmente."
                    )
                }
            }
            parsed.hasValidProducts -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        "OCR: ${parsed.productos.size} producto(s) detectado(s). Revisa nombre y precios."
                    )
                }
            }
            else -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        "OCR: total detectado. Añade los productos a mano si faltan."
                    )
                }
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
        NebulaScreenBackground(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Añadir Ticket", color = Color.White, style = MaterialTheme.typography.headlineMedium)

                ticketFields(
                    precio = precio,
                    onPrecioChange = { precio = it },
                    precioError = precioError,
                    nombre = nombre,
                    onNombreChange = { nombre = it },
                    nombreError = nombreError,
                    fecha = fecha,
                    onFechaChange = { fecha = it },
                    fechaError = fechaError,
                    categoria = categoria,
                    onCategoriaChange = { categoria = it },
                    fechaPlaceholder = "dd/mm/aaaa (opcional)"
                )

                Text("Productos", color = Color.White)

                if (productsMissingError != null) {
                    Text(productsMissingError!!, color = Color(0xFFFF8080), style = MaterialTheme.typography.bodySmall)
                }

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
                    singleProductMode = singleProductMode,
                    productErrors = productErrors
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
                        submitAttempted = true
                        if (!canSave) return@Button

                        val prepared = TicketProductHelper.prepareForSave(productos, totalValue)
                        val request = CreateTicketRequest(
                            nombre = nombre.trim(),
                            precio = totalValue,
                            categoria = categoria.trim(),
                            fecha = FormValidators.parseDate(fecha),
                            productos = prepared
                        )
                        RetrofitClient.api.createTicket(request)
                            .enqueue(object : Callback<Map<String, Any>> {
                                override fun onResponse(
                                    call: Call<Map<String, Any>>,
                                    response: Response<Map<String, Any>>
                                ) {
                                    if (response.isSuccessful) {
                                        navController.navigate("home")
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(ApiErrorParser.message(response))
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Error de conexión")
                                    }
                                }
                            })
                    },
                    enabled = canSave || !submitAttempted,
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
    precioError: String? = null,
    nombre: String,
    onNombreChange: (String) -> Unit,
    nombreError: String? = null,
    fecha: String,
    onFechaChange: (String) -> Unit,
    fechaError: String? = null,
    categoria: String,
    onCategoriaChange: (String) -> Unit,
    showFecha: Boolean = true,
    fechaPlaceholder: String = "dd/mm/aaaa"
) {
    ValidatedOutlinedField(
        value = precio,
        onValueChange = onPrecioChange,
        label = "Precio total",
        error = precioError,
        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
    )
    Spacer(Modifier.height(8.dp))
    ValidatedOutlinedField(
        value = nombre,
        onValueChange = onNombreChange,
        label = "Tienda",
        error = nombreError
    )
    if (showFecha) {
        Spacer(Modifier.height(8.dp))
        ValidatedOutlinedField(
            value = fecha,
            onValueChange = onFechaChange,
            label = "Fecha",
            error = fechaError,
            placeholder = fechaPlaceholder
        )
    }
    Spacer(Modifier.height(8.dp))
    ValidatedOutlinedField(
        value = categoria,
        onValueChange = onCategoriaChange,
        label = "Categoría"
    )
}

@Composable
fun productListFields(
    productos: List<ProductDto>,
    onProductosChange: (List<ProductDto>) -> Unit,
    singleProductMode: Boolean,
    productErrors: Map<Int, String> = emptyMap()
) {
    productos.forEachIndexed { index, item ->
        if (singleProductMode) {
            ValidatedOutlinedField(
                value = item.nombre,
                onValueChange = { newValue ->
                    onProductosChange(productos.toMutableList().also { it[index] = it[index].copy(nombre = newValue) })
                },
                label = "Producto",
                error = productErrors[index]
            )
            Spacer(Modifier.height(8.dp))
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValidatedOutlinedField(
                    value = item.nombre,
                    onValueChange = { newValue ->
                        onProductosChange(productos.toMutableList().also { it[index] = it[index].copy(nombre = newValue) })
                    },
                    label = "Producto ${index + 1}",
                    modifier = Modifier.weight(1f)
                )
                ValidatedOutlinedField(
                    value = item.precio?.toString()?.takeIf { it != "0.0" } ?: "",
                    onValueChange = { newValue ->
                        onProductosChange(
                            productos.toMutableList().also {
                                it[index] = it[index].copy(precio = newValue.replace(",", ".").toDoubleOrNull())
                            }
                        )
                    },
                    label = "Precio",
                    error = productErrors[index],
                    modifier = Modifier.width(120.dp),
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                )
            }
            Spacer(Modifier.height(8.dp))
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
