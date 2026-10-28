package com.example.barnyhealth.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DecimalInputParserTest {

    @Test
    fun `parses dot decimal`() {
        assertEquals(4.5f, DecimalInputParser.parse("4.5"))
    }

    @Test
    fun `parses comma decimal`() {
        assertEquals(4.5f, DecimalInputParser.parse("4,5"))
    }

    @Test
    fun `trims whitespace`() {
        assertEquals(4.5f, DecimalInputParser.parse(" 4,5 "))
    }

    @Test
    fun `returns null for empty input`() {
        assertNull(DecimalInputParser.parse(""))
    }

    @Test
    fun `returns null for blank input`() {
        assertNull(DecimalInputParser.parse("   "))
    }

    @Test
    fun `returns null for null input`() {
        assertNull(DecimalInputParser.parse(null))
    }

    @Test
    fun `returns null for mixed separators`() {
        assertNull(DecimalInputParser.parse("4,5.6"))
        assertNull(DecimalInputParser.parse("4.5,6"))
    }

    @Test
    fun `returns null for invalid text`() {
        assertNull(DecimalInputParser.parse("abc"))
    }

    @Test
    fun `returns null for double minus`() {
        assertNull(DecimalInputParser.parse("--5"))
    }
}
