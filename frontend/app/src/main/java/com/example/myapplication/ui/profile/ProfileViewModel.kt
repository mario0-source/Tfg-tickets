package com.example.myapplication.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.ProfileDto
import com.example.myapplication.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class ProfileUiState(
    val loading: Boolean = true,
    val profile: ProfileDto? = null,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {

    private val repository = TicketRepository()

    private val _state = MutableStateFlow(ProfileUiState())
    val state = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile(fallbackEmail: String? = null) {
        viewModelScope.launch {
            _state.value = ProfileUiState(loading = true)
            try {
                val profile = repository.getProfile()
                _state.value = ProfileUiState(loading = false, profile = profile)
            } catch (e: Exception) {
                try {
                    val tickets = repository.getTickets()
                    val total = tickets.sumOf { it.precio }
                    val count = tickets.size
                    val avg = if (count > 0) total / count else 0.0
                    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val monthly = tickets.filter { ticket ->
                        try {
                            val parts = ticket.fecha.take(7)
                            val year = parts.substring(0, 4).toInt()
                            val month = parts.substring(5, 7).toInt() - 1
                            year == currentYear && month == currentMonth
                        } catch (_: Exception) {
                            false
                        }
                    }.sumOf { it.precio }
                    val topCategory = tickets
                        .groupingBy { it.categoria.ifBlank { "General" } }
                        .eachCount()
                        .maxByOrNull { it.value }
                        ?.key ?: "General"

                    _state.value = ProfileUiState(
                        loading = false,
                        profile = ProfileDto(
                            email = fallbackEmail ?: "Usuario",
                            ticketsCount = count,
                            totalSpent = total,
                            avgSpendPerTicket = avg,
                            topCategory = topCategory,
                            monthlySpent = monthly
                        )
                    )
                } catch (inner: Exception) {
                    _state.value = ProfileUiState(
                        loading = false,
                        error = e.message ?: inner.message
                    )
                }
            }
        }
    }
}
