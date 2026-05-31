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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun EditTicketScreen(
    navController: NavHostController,
    ticketId: Int,
    viewModel: TicketDetailViewModel = viewModel()
) {
    val primaryGreen = Color(0xFF00FF85)
    val scrollState = rememberScrollState()
    val repository = remember { TicketRepository() }
    val state by viewModel.state.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var productos by remember { mutableStateOf(listOf(ProductDto("", null))) }
    var loaded by remember { mutableStateOf(false) }

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
    val validationState = remember(totalValue, productos, singleProductMode) {
        TicketTotalValidator.validate(totalValue, productos, singleProductMode)
    }
    val canSave = remember(validationState, totalValue, productos, singleProductMode, nombre) {
        nombre.isNotBlank() && totalValue > 0 && productos.any { it.nombre.isNotBlank() } &&
                (singleProductMode || validationState is TicketValidationState.Valid)
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
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
                Text("Editar Ticket", color = Color.White, fontSize = 24.sp)

                ticketFields(precio, { precio = it }, nombre, { nombre = it }, "", {}, categoria, { categoria = it })

                Text("Productos", color = Color.White)
                if (singleProductMode) {
                    Text("Un solo producto: usará el precio total", color = Color(0xFFB0B0C0), fontSize = 12.sp)
                }
                productListFields(productos, { productos = it }, singleProductMode)
                ValidationBanner(validationState) {
                    productos = TicketTotalValidator.autoAdjustProducts(productos, totalValue)
                }

                Button(
                    onClick = { productos = productos + ProductDto("", null) },
                    colors = ButtonDefaults.buttonColors(primaryGreen)
                ) { Text("+ Añadir producto", color = Color.Black) }

                Button(
                    onClick = {
                        val prepared = TicketProductHelper.prepareForSave(productos, totalValue)
                        repository.updateTicket(
                            ticketId,
                            UpdateTicketRequest(nombre, totalValue, categoria, prepared)
                        ).enqueue(object : Callback<Map<String, Any>> {
                            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                                if (response.isSuccessful) {
                                    navController.popBackStack()
                                }
                            }
                            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {}
                        })
                    },
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(primaryGreen)
                ) { Text("Guardar cambios", color = Color.Black) }
            }
        }
    }
}
