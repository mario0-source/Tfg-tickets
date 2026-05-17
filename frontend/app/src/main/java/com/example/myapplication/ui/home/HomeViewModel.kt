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

                Log.d("API_TICKETS", tickets.toString()) // 👈 DEBUG

                //val stats = repository.getStats()

                //Log.d("API_STATS", stats.toString()) // 👈 DEBUG

                _state.value = HomeUiState(
                    loading = false,
                    tickets = tickets,
                    //total = stats.total,
                    //variation = stats.variationPercent,
                    //ticketsCount = stats.ticketsCount,
                    //categoriesCount = stats.categoriesCount
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