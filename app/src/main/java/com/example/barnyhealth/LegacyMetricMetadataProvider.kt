package com.example.barnyhealth

import android.graphics.Color

data class LegacyMetricMetadata(
    val displayName: String,
    val abbreviation: String,
    val description: String,
    val color: Int,
    val normMin: Float?,
    val normMax: Float?,
    val unit: String
)

class LegacyMetricMetadataProvider {

    fun getByKey(key: String): LegacyMetricMetadata? {
        val norms = HealthParams.NORMS[key]

        return LegacyMetricMetadata(
            displayName = key,
            abbreviation = HealthParams.ABBREVIATIONS[key]?.toString() ?: key,
            description = HealthParams.DESCRIPTIONS[key]?.toString()
                ?: "Описание пока не добавлено.",
            color = HealthParams.COLORS[key] ?: Color.GRAY,
            normMin = norms?.first,
            normMax = norms?.second,
            unit = ""
        )
    }
}