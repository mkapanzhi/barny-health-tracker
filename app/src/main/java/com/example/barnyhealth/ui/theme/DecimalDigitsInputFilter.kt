package com.example.barnyhealth

import android.text.InputFilter
import android.text.Spanned

class DecimalDigitsInputFilter(
    private val digitsAfterDecimal: Int = 1
) : InputFilter {

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val newValue = StringBuilder(dest)
            .replace(dstart, dend, source.subSequence(start, end).toString())
            .toString()
            .replace(',', '.')

        if (newValue.isEmpty()) return null
        if (newValue.count { it == '.' } > 1) return ""

        val parts = newValue.split('.')
        if (parts.size > 1 && parts[1].length > digitsAfterDecimal) return ""

        return null
    }
}