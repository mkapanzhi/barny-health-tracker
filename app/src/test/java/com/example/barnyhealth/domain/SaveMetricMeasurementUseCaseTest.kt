package com.example.barnyhealth.domain

import com.example.barnyhealth.DataRepository
import com.example.barnyhealth.domain.model.MetricSource
import com.example.barnyhealth.domain.model.MetricUiModel
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class SaveMetricMeasurementUseCaseTest {
    private val dataRepository = mockk<DataRepository>()
    private val addMeasurement = mockk<AddMeasurementByMetricCodeUseCase>()
    private val saveMeasurement = SaveMetricMeasurementUseCase(dataRepository, addMeasurement)
    private val measuredAt = 1_700_000_000_000L
    private val value = 12.5f

    @Test
    fun `missing model throws without writing to either storage`() = runBlocking {
        val error = expectIllegalState {
            saveMeasurement(null, "fallback_key", value, measuredAt, "quick_add")
        }

        assertEquals("Metric model is missing for param: fallback_key", error.message)
        verifyNoWrites()
    }

    @Test
    fun `room metric is saved only through room use case`() = runBlocking {
        val model = metric(MetricSource.ROOM, "room_code")
        coEvery {
            addMeasurement("room_code", value.toDouble(), "unit", measuredAt, null, "quick_add")
        } returns 1L

        saveMeasurement(model, "different_fallback", value, measuredAt, "quick_add")

        coVerify(exactly = 1) {
            addMeasurement("room_code", value.toDouble(), "unit", measuredAt, null, "quick_add")
        }
        verify { dataRepository wasNot Called }
    }

    @Test
    fun `room metric with null code throws without writing`() = runBlocking {
        assertInvalidRoomCode(null)
    }

    @Test
    fun `room metric with empty code throws without writing`() = runBlocking {
        assertInvalidRoomCode("")
    }

    @Test
    fun `room metric with whitespace code throws without writing`() = runBlocking {
        assertInvalidRoomCode("   ")
    }

    @Test
    fun `legacy metric uses model key instead of fallback key`() = runBlocking {
        val model = metric(MetricSource.LEGACY, null)
        every { dataRepository.upsertLegacyMeasurementForDay("model_key", measuredAt, value) } returns Unit

        saveMeasurement(model, "different_fallback", value, measuredAt, "bulk_entry")

        verify(exactly = 1) {
            dataRepository.upsertLegacyMeasurementForDay("model_key", measuredAt, value)
        }
        verify(exactly = 0) {
            dataRepository.upsertLegacyMeasurementForDay("different_fallback", any(), any())
        }
        verify { addMeasurement wasNot Called }
    }

    @Test
    fun `room save failure propagates without legacy fallback`() = runBlocking {
        val failure = IllegalStateException("Room write failed")
        coEvery { addMeasurement(any(), any(), any(), any(), any(), any()) } throws failure

        val actual = expectIllegalState {
            saveMeasurement(metric(MetricSource.ROOM, "room_code"), "fallback", value, measuredAt, "bulk_entry")
        }

        assertSame(failure, actual)
        verify { dataRepository wasNot Called }
    }

    @Test
    fun `legacy save failure propagates without room fallback`() = runBlocking {
        val failure = IllegalStateException("Legacy write failed")
        every { dataRepository.upsertLegacyMeasurementForDay(any(), any(), any()) } throws failure

        val actual = expectIllegalState {
            saveMeasurement(metric(MetricSource.LEGACY, null), "fallback", value, measuredAt, "bulk_entry")
        }

        assertSame(failure, actual)
        verify { addMeasurement wasNot Called }
    }

    @Test
    fun `room cancellation propagates without legacy fallback`() = runBlocking {
        val cancellation = CancellationException("Cancelled")
        coEvery { addMeasurement(any(), any(), any(), any(), any(), any()) } throws cancellation

        try {
            saveMeasurement(metric(MetricSource.ROOM, "room_code"), "fallback", value, measuredAt, "quick_add")
            throw AssertionError("Expected CancellationException")
        } catch (actual: CancellationException) {
            assertSame(cancellation, actual)
        }
        verify { dataRepository wasNot Called }
    }

    private suspend fun assertInvalidRoomCode(code: String?) {
        val error = expectIllegalState {
            saveMeasurement(metric(MetricSource.ROOM, code), "fallback", value, measuredAt, "quick_add")
        }
        assertEquals("Room metric code is missing or blank for model_key", error.message)
        verifyNoWrites()
    }

    private fun verifyNoWrites() {
        verify {
            dataRepository wasNot Called
            addMeasurement wasNot Called
        }
    }

    private suspend fun expectIllegalState(block: suspend () -> Unit): IllegalStateException {
        try {
            block()
        } catch (error: IllegalStateException) {
            return error
        }
        throw AssertionError("Expected IllegalStateException")
    }

    private fun metric(source: MetricSource, code: String?) = MetricUiModel(
        key = "model_key",
        displayName = "Test metric",
        abbreviation = "TM",
        description = "Test metric description",
        color = 0,
        unit = "unit",
        normMin = null,
        normMax = null,
        source = source,
        roomMetricCode = code
    )
}
