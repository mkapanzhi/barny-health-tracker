package com.barny.healthtracker.events

import org.junit.Assert.assertEquals
import org.junit.Test

class EventDatesTest {
    @Test fun leapFebruary() { assertEquals(20240229, EventDates.last(20240201)) }
    @Test fun regularFebruary() { assertEquals(20250228, EventDates.last(20250201)) }
    @Test fun nextYear() { assertEquals(20270101, EventDates.shift(20261201, 1)) }
    @Test fun previousYear() { assertEquals(20251201, EventDates.shift(20260101, -1)) }
    @Test fun mondayFirst() { assertEquals(0, EventDates.offset(20260601)) }
    @Test fun sundayOffset() { assertEquals(6, EventDates.offset(20260301)) }
    @Test fun dateRoundTrip() { assertEquals(20261019, EventDates.key(EventDates.calendar(20261019))) }
}
