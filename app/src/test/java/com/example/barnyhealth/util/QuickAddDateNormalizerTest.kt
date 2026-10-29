package com.example.barnyhealth.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class QuickAddDateNormalizerTest {

    private val timeZoneIds = listOf(
        "UTC",
        "Europe/Minsk",
        "America/Los_Angeles",
        "Asia/Kolkata",
        "Asia/Kathmandu",
        "Pacific/Kiritimati",
        "Pacific/Pago_Pago"
    )

    @Test
    fun `picker selection becomes the same local calendar day`() {
        val selection = timestamp("UTC", 2026, 1, 15)

        timeZoneIds.forEach { zoneId ->
            val actual = QuickAddDateNormalizer.fromPickerSelection(
                selection,
                TimeZone.getTimeZone(zoneId)
            )

            assertEquals(zoneId, timestamp(zoneId, 2026, 1, 15), actual)
        }
    }

    @Test
    fun `local date becomes the same UTC calendar day for picker`() {
        val expected = timestamp("UTC", 2026, 1, 15)

        timeZoneIds.forEach { zoneId ->
            val localTimestamp = timestamp(
                zoneId, 2026, 1, 15, 23, 59, 58, 999
            )
            val actual = QuickAddDateNormalizer.toPickerSelection(
                localTimestamp,
                TimeZone.getTimeZone(zoneId)
            )

            assertEquals(zoneId, expected, actual)
        }
    }

    @Test
    fun `initial timestamp is normalized to local start of day`() {
        timeZoneIds.forEach { zoneId ->
            val localTimestamp = timestamp(
                zoneId, 2026, 1, 15, 14, 35, 42, 123
            )
            val actual = QuickAddDateNormalizer.toLocalStartOfDay(
                localTimestamp,
                TimeZone.getTimeZone(zoneId)
            )

            assertEquals(zoneId, timestamp(zoneId, 2026, 1, 15), actual)
        }
    }

    @Test
    fun `picker round trip preserves the date at calendar boundaries`() {
        val dates = listOf(
            Triple(2026, 1, 1),
            Triple(2026, 1, 31),
            Triple(2024, 2, 29),
            Triple(2026, 12, 31)
        )

        timeZoneIds.forEach { zoneId ->
            val timeZone = TimeZone.getTimeZone(zoneId)

            dates.forEach { (year, month, day) ->
                val selection = timestamp("UTC", year, month, day)
                val localTimestamp = QuickAddDateNormalizer.fromPickerSelection(
                    selection,
                    timeZone
                )

                assertEquals(
                    "$zoneId: $year-$month-$day",
                    timestamp(zoneId, year, month, day),
                    localTimestamp
                )
                assertEquals(
                    "$zoneId: round trip $year-$month-$day",
                    selection,
                    QuickAddDateNormalizer.toPickerSelection(localTimestamp, timeZone)
                )
            }
        }
    }

    @Test
    fun `conversion uses the offset of the selected date across DST`() {
        val zoneId = "America/Los_Angeles"
        val timeZone = TimeZone.getTimeZone(zoneId)
        val dates = listOf(
            Triple(2026, 3, 8),
            Triple(2026, 3, 9),
            Triple(2026, 11, 1),
            Triple(2026, 11, 2)
        )

        dates.forEach { (year, month, day) ->
            val selection = timestamp("UTC", year, month, day)
            val localTimestamp = QuickAddDateNormalizer.fromPickerSelection(
                selection,
                timeZone
            )

            assertEquals(
                "$year-$month-$day",
                timestamp(zoneId, year, month, day),
                localTimestamp
            )
            assertEquals(
                selection,
                QuickAddDateNormalizer.toPickerSelection(localTimestamp, timeZone)
            )
        }
    }

    private fun timestamp(
        zoneId: String,
        year: Int,
        month: Int,
        day: Int,
        hour: Int = 0,
        minute: Int = 0,
        second: Int = 0,
        millisecond: Int = 0
    ): Long {
        return Calendar.getInstance(TimeZone.getTimeZone(zoneId)).apply {
            clear()
            set(year, month - 1, day, hour, minute, second)
            set(Calendar.MILLISECOND, millisecond)
        }.timeInMillis
    }
}
