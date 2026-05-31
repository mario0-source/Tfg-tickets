package com.example.myapplication.ui.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.TicketDto
import com.example.myapplication.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TicketsUiState(

    val loading: Boolean = false,

    val tickets: List<TicketDto> = emptyList(),

    val total: Double = 0.0,

    val ticketsCount: Int = 0,

    val error: String? = null
)

class TicketsViewModel : ViewModel() {

    private val repository = TicketRepository()

    private val _state = MutableStateFlow(TicketsUiState())

    val state = _state.asStateFlow()

    init {
        loadTickets()
    }

    fun loadTickets() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            try {
                val tickets = repository.getTickets()
                val total = tickets.sumOf { it.precio }

                _state.value = TicketsUiState(
                    tickets = tickets,
                    total = total,
                    ticketsCount = tickets.size
                )
            } catch (e: Exception) {
                _state.value = TicketsUiState(error = e.message)
            }
        }
    }

    fun getTicketById(id: Int): TicketDto? =
        _state.value.tickets.find { it.id == id }
}
