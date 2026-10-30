package com.barny.healthtracker.events

import java.util.Calendar
import java.util.GregorianCalendar

object EventDates {
    fun calendar(key: Int): Calendar = GregorianCalendar().apply {
        clear()
        set(key / 10000, key / 100 % 100 - 1, key % 100, 12, 0, 0)
    }
    fun key(calendar: Calendar): Int = calendar.get(Calendar.YEAR) * 10000 +
        (calendar.get(Calendar.MONTH) + 1) * 100 + calendar.get(Calendar.DAY_OF_MONTH)
    fun today(): Int = key(Calendar.getInstance())
    fun month(key: Int): Int = key / 100 * 100 + 1
    fun last(key: Int): Int = calendar(month(key)).let {
        it.set(Calendar.DAY_OF_MONTH, it.getActualMaximum(Calendar.DAY_OF_MONTH))
        key(it)
    }
    fun shift(key: Int, delta: Int): Int = calendar(month(key)).let {
        it.add(Calendar.MONTH, delta)
        key(it)
    }
    fun offset(key: Int): Int = (calendar(month(key)).get(Calendar.DAY_OF_WEEK) + 5) % 7
}
