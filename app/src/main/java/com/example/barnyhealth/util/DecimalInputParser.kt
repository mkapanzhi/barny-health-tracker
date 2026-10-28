package com.example.barnyhealth.util

object DecimalInputParser {

    fun parse(text: String?): Float? {
        val normalized = text
            ?.trim()
            ?.replace(",", ".")
            .orEmpty()

        if (normalized.isEmpty()) return null

        return normalized.toFloatOrNull()
    }
}
