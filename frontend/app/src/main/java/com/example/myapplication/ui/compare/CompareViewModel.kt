package com.example.myapplication.ui.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.CompareProductSummary
import com.example.myapplication.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CompareUiState(
    val loading: Boolean = true,
    val products: List<CompareProductSummary> = emptyList(),
    val ticketsWithProducts: Int = 0,
    val error: String? = null
)

class CompareViewModel : ViewModel() {

    private val repository = TicketRepository()

    private val _state = MutableStateFlow(CompareUiState())
    val state = _state.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _state.value = CompareUiState(loading = true)
            try {
                val products = repository.getCompareProducts()
                val tickets = repository.getTickets()
                val withProducts = tickets.count { it.productos.isNotEmpty() }

                _state.value = CompareUiState(
                    loading = false,
                    products = products,
                    ticketsWithProducts = withProducts
                )
            } catch (e: Exception) {
                _state.value = CompareUiState(
                    loading = false,
                    error = e.message
                )
            }
        }
    }
}
