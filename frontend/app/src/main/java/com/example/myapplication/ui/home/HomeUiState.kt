package com.example.myapplication.ui.home

import com.example.myapplication.model.TicketDto

data class HomeUiState(

    val loading: Boolean = false,
    val tickets: List<TicketDto> = emptyList(),

    val total: Double = 0.0,
    val variation: Double = 0.0,
    val ticketsCount: Int = 0,
    val categoriesCount: Int = 0,

    val error: String? = null
)