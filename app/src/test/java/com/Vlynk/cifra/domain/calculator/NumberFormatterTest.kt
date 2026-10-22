package com.Vlynk.cifra.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class NumberFormatterTest {

    private lateinit var formatter: NumberFormatter

    @Before
    fun setUp() {
        formatter = NumberFormatter()
    }

    // --- Trailing zeros removal ---

    @Test
    fun `14_00 formats to 14`() {
        assertEquals("14", formatter.format(BigDecimal("14.00")))
    }

    @Test
    fun `0_30 formats to 0_3`() {
        assertEquals("0.3", formatter.format(BigDecimal("0.30")))
    }

    @Test
    fun `integer stays integer`() {
        assertEquals("42", formatter.format(BigDecimal("42")))
    }

    @Test
    fun `negative trailing zeros stripped`() {
        assertEquals("-7", formatter.format(BigDecimal("-7.0000")))
    }

    // --- No scientific notation ---

    @Test
    fun `10 does not become 1E+1`() {
        assertEquals("10", formatter.format(BigDecimal("10")))
    }

    @Test
    fun `large number does not use scientific notation`() {
        assertEquals("1000000", formatter.format(BigDecimal("1000000")))
    }

    @Test
    fun `stripTrailingZeros does not produce scientific notation for large values`() {
        val bd = BigDecimal("1000000").stripTrailingZeros()
        // Verify our formatter avoids "1E+6"
        assertEquals("1000000", formatter.format(bd))
    }

    // --- Thousands separator ---

    @Test
    fun `1000000 formats with thousands separator`() {
        assertEquals("1,000,000", formatter.formatWithThousands(BigDecimal("1000000")))
    }

    @Test
    fun `1234567_89 formats with thousands separator and decimals`() {
        assertEquals("1,234,567.89", formatter.formatWithThousands(BigDecimal("1234567.89")))
    }

    @Test
    fun `negative number with thousands separator`() {
        assertEquals("-1,234,567", formatter.formatWithThousands(BigDecimal("-1234567")))
    }

    @Test
    fun `small number unaffected by thousands separator`() {
        assertEquals("999", formatter.formatWithThousands(BigDecimal("999")))
    }

    // --- Decimal precision ---

    @Test
    fun `value with more than max display digits is truncated`() {
        val bd = BigDecimal("1.23456789012345")
        val result = formatter.format(bd, maxDecimalDigits = 10)
        // Should not exceed 10 decimal digits in display
        val decimalPart = result.substringAfter(".", "")
        assert(decimalPart.length <= 10) { "Expected at most 10 decimal digits, got ${decimalPart.length}" }
    }
}
