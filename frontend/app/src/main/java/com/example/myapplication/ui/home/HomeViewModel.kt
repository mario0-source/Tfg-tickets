package com.example.myapplication.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = TicketRepository()

    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {



            viewModelScope.launch {

                try {

                    _state.value = _state.value.copy(loading = true)

                    val tickets = repository.getTickets()

                    val stats = repository.getStats()

                    _state.value = HomeUiState(
                        loading = false,
                        tickets = tickets,
                        total = stats.total,
                        variation = stats.variationPercent,
                        ticketsCount = stats.ticketsCount,
                        categoriesCount = stats.categoriesCount
                    )

                } catch (e: Exception) {

                    Log.e("API_ERROR", e.message ?: "error")

                    _state.value = HomeUiState(
                        loading = false,
                        error = e.message
                    )
                }
            }
        }

}