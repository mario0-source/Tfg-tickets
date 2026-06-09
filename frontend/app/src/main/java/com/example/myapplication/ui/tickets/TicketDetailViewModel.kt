package com.example.myapplication.ui.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.TicketDto
import com.example.myapplication.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TicketDetailUiState(
    val loading: Boolean = true,
    val ticket: TicketDto? = null,
    val error: String? = null
)

class TicketDetailViewModel : ViewModel() {

    private val repository = TicketRepository()

    private val _state = MutableStateFlow(TicketDetailUiState())
    val state = _state.asStateFlow()

    fun loadTicket(ticketId: Int) {
        viewModelScope.launch {
            _state.value = TicketDetailUiState(loading = true)
            try {
                val ticket = repository.getTicketById(ticketId)
                _state.value = TicketDetailUiState(loading = false, ticket = ticket)
            } catch (e: Exception) {
                _state.value = TicketDetailUiState(
                    loading = false,
                    error = e.message ?: "Error al cargar ticket"
                )
            }
        }
    }

    fun deleteTicket(ticketId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteTicket(ticketId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al eliminar el ticket")
            }
        }
    }
}
