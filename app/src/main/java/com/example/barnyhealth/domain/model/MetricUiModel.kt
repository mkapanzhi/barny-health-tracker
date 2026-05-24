package com.example.barnyhealth.domain.model

data class MetricUiModel(
    val key: String,
    val displayName: String,
    val abbreviation: String,
    val description: String,
    val color: Int,
    val unit: String,
    val normMin: Float?,
    val normMax: Float?,
    val source: MetricSource,
    val roomMetricCode: String?
)

enum class MetricSource {
    ROOM,
    LEGACY
}