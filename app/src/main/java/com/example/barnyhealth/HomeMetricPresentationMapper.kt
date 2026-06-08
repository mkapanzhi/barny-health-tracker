package com.example.barnyhealth

import android.graphics.Color
import com.example.barnyhealth.domain.model.MetricUiModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeMetricPresentationMapper {

    fun resolveNorms(
        model: MetricUiModel?,
        roomNorms: Pair<Float, Float>?
    ): Pair<Float, Float>? {
        return when {
            roomNorms != null -> roomNorms
            model?.normMin != null && model.normMax != null -> model.normMin to model.normMax
            else -> null
        }
    }

    fun resolveMetricColor(model: MetricUiModel?): Int {
        return model?.color ?: Color.GRAY
    }

    fun resolveChartLabel(param: String, model: MetricUiModel?): String {
        return model?.displayName ?: if (param.isNotBlank()) param else "Нет данных"
    }

    private fun toCompactLabel(text: String): String {
        return text.substringBefore(" (")
            .substringBefore("(")
            .trim()
    }

    fun resolveMeasurementParamLabel(param: String, model: MetricUiModel?): String {
        val displayName = model?.displayName ?: param
        return toCompactLabel(displayName)
    }

    fun mapMeasurementItems(
        param: String,
        model: MetricUiModel?,
        roomNorms: Pair<Float, Float>?,
        pairedData: List<Pair<Long, Float>>
    ): List<MeasurementItem> {
        val norms = resolveNorms(model, roomNorms)
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val paramLabel = resolveMeasurementParamLabel(param, model)
        val unit = model?.unit ?: ""

        return pairedData
            .sortedByDescending { it.first }
            .map { (timestamp, value) ->
                val isOutOfNorm = norms?.let { value < it.first || value > it.second } ?: false

                MeasurementItem(
                    timestamp = timestamp,
                    date = dateFormat.format(Date(timestamp)),
                    param = paramLabel,
                    value = String.format(Locale.US, "%.1f", value),
                    unit = unit,
                    isOutOfNorm = isOutOfNorm
                )
            }
    }

    fun buildNormText(
        model: MetricUiModel?,
        roomNorms: Pair<Float, Float>?
    ): String {
        val normPair = resolveNorms(model, roomNorms)

        return if (normPair != null) {
            val unitSuffix = model?.unit
                ?.takeIf { it.isNotBlank() }
                ?.let { " $it" }
                ?: ""

            "Норма: ${
                String.format(Locale.US, "%.1f", normPair.first)
            }–${
                String.format(Locale.US, "%.1f", normPair.second)
            }$unitSuffix"
        } else {
            "Норма неизвестна"
        }
    }
}