package com.example.barnyhealth.util

import java.util.Calendar
import java.util.TimeZone

internal object QuickAddDateNormalizer {

    /** Converts an instant to the start of its local calendar day. */
    fun toLocalStartOfDay(
        timestamp: Long,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Long = copyDate(timestamp, timeZone, timeZone)

    /** Converts the UTC picker date to the same local calendar date. */
    fun fromPickerSelection(
        selection: Long,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Long = copyDate(selection, TimeZone.getTimeZone("UTC"), timeZone)

    /** Converts a local calendar date to the UTC date expected by the picker. */
    fun toPickerSelection(
        timestamp: Long,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Long = copyDate(timestamp, timeZone, TimeZone.getTimeZone("UTC"))

    private fun copyDate(
        timestamp: Long,
        sourceTimeZone: TimeZone,
        targetTimeZone: TimeZone
    ): Long {
        val source = Calendar.getInstance(sourceTimeZone).apply {
            timeInMillis = timestamp
        }

        return Calendar.getInstance(targetTimeZone).apply {
            clear()
            set(
                source.get(Calendar.YEAR),
                source.get(Calendar.MONTH),
                source.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0
            )
        }.timeInMillis
    }
}
