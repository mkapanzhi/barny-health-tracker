package com.barny.healthtracker.ui.weightdebug

data class WeightDebugUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val historyText: String = "История веса пока не загружена"
)