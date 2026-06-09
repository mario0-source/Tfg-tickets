package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.model.ProductDto
import com.example.myapplication.model.UpdateTicketRequest
import com.example.myapplication.repository.TicketRepository
import com.example.myapplication.ui.tickets.TicketDetailViewModel
import com.example.myapplication.util.TicketProductHelper
import com.example.myapplication.util.TicketTotalValidator
import com.example.myapplication.util.TicketValidationState
import com.example.myapplication.ui.theme.NebulaGreen
import com.example.myapplication.ui.theme.NebulaScreenBackground
import com.example.myapplication.util.ApiErrorParser
import com.example.myapplication.util.FormValidators
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun EditTicketScreen(
    navController: NavHostController,
    ticketId: Int,
    viewModel: TicketDetailViewModel = viewModel()
) {
    val primaryGreen = NebulaGreen
    val scrollState = rememberScrollState()
    val repository = remember { TicketRepository() }
    val state by viewModel.state.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var productos by remember { mutableStateOf(listOf(ProductDto("", null))) }
    var loaded by remember { mutableStateOf(false) }
    var submitAttempted by remember { mutableStateOf(false) }

    LaunchedEffect(ticketId) { viewModel.loadTicket(ticketId) }

    LaunchedEffect(state.ticket) {
        state.ticket?.let { ticket ->
            if (!loaded) {
                nombre = ticket.nombre
                precio = ticket.precio.toString()
                categoria = ticket.categoria
                productos = ticket.productos.ifEmpty { listOf(ProductDto("", null)) }
                loaded = true
            }
        }
    }

    val singleProductMode = productos.size == 1
    val totalValue = precio.replace(",", ".").toDoubleOrNull() ?: 0.0

    val nombreError = remember(nombre, submitAttempted) {
        if (!submitAttempted && nombre.isBlank()) null else FormValidators.validateRequired(nombre, "La tienda")
    }
    val precioError = remember(precio, submitAttempted) {
        if (!submitAttempted && precio.isBlank()) null else FormValidators.validatePrice(precio)
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
        nombreError, precioError, productErrors, productsMissingError,
        validationState, singleProductMode
    ) {
        nombreError == null &&
                precioError == null &&
                productErrors.isEmpty() &&
                productsMissingError == null &&
                FormValidators.hasAnyProductName(productos) &&
                (singleProductMode || validationState is TicketValidationState.Valid)
    }

    Scaffold(
        containerColor = Color.Transparent,
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
                Text("Editar Ticket", color = Color.White, style = MaterialTheme.typography.headlineMedium)

                ticketFields(
                    precio, { precio = it }, precioError,
                    nombre, { nombre = it }, nombreError,
                    "", {}, null,
                    categoria, { categoria = it },
                    showFecha = false
                )

                Text("Productos", color = Color.White)
                if (productsMissingError != null) {
                    Text(productsMissingError!!, color = Color(0xFFFF8080), style = MaterialTheme.typography.bodySmall)
                }
                if (singleProductMode) {
                    Text("Un solo producto: usará el precio total", color = Color(0xFFB0B0C0), fontSize = 12.sp)
                }
                productListFields(productos, { productos = it }, singleProductMode, productErrors)
                ValidationBanner(validationState) {
                    productos = TicketTotalValidator.autoAdjustProducts(productos, totalValue)
                }

                Button(
                    onClick = { productos = productos + ProductDto("", null) },
                    colors = ButtonDefaults.buttonColors(primaryGreen)
                ) { Text("+ Añadir producto", color = Color.Black) }

                Button(
                    onClick = {
                        submitAttempted = true
                        if (!canSave) return@Button

                        val prepared = TicketProductHelper.prepareForSave(productos, totalValue)
                        repository.updateTicket(
                            ticketId,
                            UpdateTicketRequest(nombre.trim(), totalValue, categoria.trim(), prepared)
                        ).enqueue(object : Callback<Map<String, Any>> {
                            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                                if (response.isSuccessful) {
                                    navController.popBackStack()
                                }
                            }
                            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {}
                        })
                    },
                    enabled = canSave || !submitAttempted,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(primaryGreen)
                ) { Text("Guardar cambios", color = Color.Black) }
            }
        }
    }
}
