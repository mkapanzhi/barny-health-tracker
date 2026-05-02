package com.example.barnyhealth.ui.weightdebug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.barnyhealth.domain.AddWeightMeasurementUseCase
import com.example.barnyhealth.domain.GetWeightHistoryUseCase

class WeightDebugViewModel(
    private val addWeightMeasurementUseCase: AddWeightMeasurementUseCase,
    private val getWeightHistoryUseCase: GetWeightHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightDebugUiState())
    val uiState: StateFlow<WeightDebugUiState> = _uiState.asStateFlow()

    fun addTestWeight() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                message = "Добавляю тестовый вес..."
            )

            try {
                val id = addWeightMeasurementUseCase(
                    weightKg = 4.2,
                    measuredAt = System.currentTimeMillis(),
                    note = "Debug weight"
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Вес добавлен, id=$id"
                )

                loadHistory()
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Ошибка добавления: ${t.message}"
                )
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                message = "Читаю историю веса..."
            )

            try {
                val history = getWeightHistoryUseCase()

                val text = if (history.isEmpty()) {
                    "История веса пустая"
                } else {
                    history.joinToString("\n") { item ->
                        "id=${item.id}, value=${item.value}, unit=${item.unit}, note=${item.note}"
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "История загружена: ${history.size} записей",
                    historyText = text
                )
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Ошибка чтения: ${t.message}"
                )
            }
        }
    }
}