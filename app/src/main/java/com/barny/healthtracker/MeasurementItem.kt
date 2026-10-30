package com.barny.healthtracker

data class MeasurementItem(
    val timestamp: Long,
    val date: String,
    val param: String,
    val value: String,
    val unit: String = "",
    val isOutOfNorm: Boolean = false
)