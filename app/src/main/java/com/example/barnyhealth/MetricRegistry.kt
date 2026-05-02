package com.example.barnyhealth

data class RoomMetricConfig(
    val metricCode: String,
    val unit: String
)

object MetricRegistry {

    private val roomMetrics = mapOf(
        "Температура" to RoomMetricConfig(
            metricCode = "temperature",
            unit = "°C"
        ),
        "Вес" to RoomMetricConfig(
            metricCode = "weight",
            unit = "kg"
        ),
        "Глюкоза" to RoomMetricConfig(
            metricCode = "glucose",
            unit = "mmol/L"
        ),
        "Мочевина" to RoomMetricConfig(
            metricCode = "urea",
            unit = "mmol/L"
        ),
        "Креатинин" to RoomMetricConfig(
            metricCode = "creatinine",
            unit = "µmol/L"
        )
    )

    fun getRoomMetricConfig(param: String): RoomMetricConfig? {
        return roomMetrics.entries.firstOrNull { (key, _) ->
            key.equals(param, ignoreCase = true)
        }?.value
    }

    fun getMetricCodeOrNull(param: String): String? {
        return getRoomMetricConfig(param)?.metricCode
    }

    fun getUnitOrNull(param: String): String? {
        return getRoomMetricConfig(param)?.unit
    }

    fun isRoomBacked(param: String): Boolean {
        return getRoomMetricConfig(param) != null
    }
}