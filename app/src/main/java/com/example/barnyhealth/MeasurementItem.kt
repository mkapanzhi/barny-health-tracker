package com.example.barnyhealth

data class MeasurementItem(
    val date: String,
    val paramName: String,
    val valueText: String,
    val isOutOfNorm: Boolean
)