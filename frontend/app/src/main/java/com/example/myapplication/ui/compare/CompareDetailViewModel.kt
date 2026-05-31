package com.example.myapplication.ui.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.CompareProductDetail
import com.example.myapplication.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CompareDetailUiState(
    val loading: Boolean = true,
    val detail: CompareProductDetail? = null,
    val error: String? = null,
    val saving: Boolean = false
)

class CompareDetailViewModel : ViewModel() {

    private val repository = TicketRepository()

    private val _state = MutableStateFlow(CompareDetailUiState())
    val state = _state.asStateFlow()

    fun loadProduct(productName: String, showLoading: Boolean = true) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = showLoading, saving = false)
            try {
                val detail = repository.getCompareProductDetail(productName)
                _state.value = CompareDetailUiState(loading = false, detail = detail)
            } catch (e: Exception) {
                _state.value = CompareDetailUiState(
                    loading = false,
                    error = e.message ?: "Error al cargar producto"
                )
            }
        }
    }

    fun addManualEntry(productName: String, store: String, price: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true)
            try {
                repository.addManualCompareEntry(productName, store, price)
                loadProduct(productName, showLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    saving = false,
                    error = e.message
                )
            }
        }
    }
}
