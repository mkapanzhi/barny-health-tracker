package com.example.barnyhealth

data class MeasurementItem(
    val timestamp: Long,
    val date: String,
    val paramName: String,
    val valueText: String,
    val isOutOfNorm: Boolean,
    val showDelete: Boolean = false
)