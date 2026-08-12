/*
 *
 *    Copyright 1996-2026 Traction Software, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

// PLEASE DO NOT DELETE THIS LINE - make copyright depends on it.

package com.tractionsoftware.commons.text;

import org.junit.jupiter.api.Test;

import java.text.NumberFormat;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public final class NumberFormatsTest {

    // =====================================================================
    // getFormattedByteSize
    // =====================================================================

    @Test
    void getFormattedByteSize_bytes_showsB() {
        String result = NumberFormats.getFormattedByteSize(500.0);
        assertTrue(result.contains("B"), result);
        assertFalse(result.contains("KB"), result);
    }

    @Test
    void getFormattedByteSize_kilobytes_showsKB() {
        String result = NumberFormats.getFormattedByteSize(2048.0);
        assertTrue(result.contains("KB"), result);
    }

    @Test
    void getFormattedByteSize_megabytes_showsMB() {
        String result = NumberFormats.getFormattedByteSize(2 * 1024 * 1024.0);
        assertTrue(result.contains("MB"), result);
    }

    @Test
    void getFormattedByteSize_gigabytes_showsGB() {
        String result = NumberFormats.getFormattedByteSize(2L * 1024 * 1024 * 1024);
        assertTrue(result.contains("GB"), result);
    }

    @Test
    void getFormattedByteSize_withUnitIndex_startsAtUnit() {
        // starting at unitIndex=1 (KB), 1.0 → "1 KB" instead of "1,024 B"
        String result = NumberFormats.getFormattedByteSize(1.0, 1);
        assertTrue(result.contains("KB"), result);
    }

    @Test
    void getFormattedByteSize_zero_showsBytes() {
        String result = NumberFormats.getFormattedByteSize(0.0);
        assertTrue(result.contains("B"), result);
    }

    // =====================================================================
    // getFormattedPercentage
    // =====================================================================

    @Test
    void getFormattedPercentage_fraction_returnsPercent() {
        String result = NumberFormats.getFormattedPercentage(0.5, 0);
        assertNotNull(result);
        assertTrue(result.contains("%"), result);
    }

    @Test
    void getFormattedPercentage_numeratorDivisor_returnsPercent() {
        String result = NumberFormats.getFormattedPercentage(1.0, 2.0);
        assertNotNull(result);
        assertTrue(result.contains("%"), result);
    }

    @Test
    void getFormattedPercentage_zeroDivisor_returnsNull() {
        // Division by zero results in NaN/Infinity; implementation handles this
        assertNull(NumberFormats.getFormattedPercentage(1.0, 0.0));
    }

    @Test
    void getFormattedPercentage_withDecimalDigits_includesDecimals() {
        String result = NumberFormats.getFormattedPercentage(0.333, 2);
        assertNotNull(result);
        assertTrue(result.contains("%"), result);
    }

    @Test
    void getFormattedPercentage_100percent() {
        String result = NumberFormats.getFormattedPercentage(1.0, 0);
        assertNotNull(result);
        assertTrue(result.contains("%"), result);
        // Should contain "100"
        assertTrue(result.contains("100"), result);
    }

    // =====================================================================
    // getDecimalFormat
    // =====================================================================

    @Test
    void getDecimalFormat_returnsNotNull() {
        NumberFormat fmt = NumberFormats.getDecimalFormat(2);
        assertNotNull(fmt);
    }

    @Test
    void getDecimalFormat_respectsFractionDigits() {
        NumberFormat fmt = NumberFormats.getDecimalFormat(2);
        assertEquals(2, fmt.getMaximumFractionDigits());
    }

    // =====================================================================
    // getFormattedWholeNumber
    // =====================================================================

    @Test
    void getFormattedWholeNumber_long_returnsFormatted() {
        String result = NumberFormats.getFormattedWholeNumber(1000L);
        assertNotNull(result);
        assertTrue(result.contains("1"), result);
        assertFalse(result.contains("."), result);
    }

    @Test
    void getFormattedWholeNumber_double_returnsFormatted() {
        String result = NumberFormats.getFormattedWholeNumber(42.7);
        assertNotNull(result);
        // integer format rounds, no decimal point
        assertFalse(result.contains("."), result);
    }

    @Test
    void getFormattedWholeNumber_zero() {
        String result = NumberFormats.getFormattedWholeNumber(0L);
        assertNotNull(result);
        assertTrue(result.contains("0"), result);
    }

    // =====================================================================
    // getWholeNumberFormat
    // =====================================================================

    @Test
    void getWholeNumberFormat_returnsNotNull() {
        assertNotNull(NumberFormats.getWholeNumberFormat());
    }

    @Test
    void getWholeNumberFormat_maxFractionDigitsIsZero() {
        assertEquals(0, NumberFormats.getWholeNumberFormat().getMaximumFractionDigits());
    }

    // =====================================================================
    // getFormattedSize (generic)
    // =====================================================================

    @Test
    void getFormattedSize_customUnits_formatsCorrectly() {
        String[] units = { " apples", " bushels" };
        NumberFormat fmt = NumberFormat.getInstance(Locale.US);
        // 50 apples (< 100 divisor)
        String result = NumberFormats.getFormattedSize(units, 100.0, fmt, 50.0, 0);
        assertTrue(result.contains("apples"), result);
    }

    @Test
    void getFormattedSize_exceedsDivisor_advancesUnit() {
        String[] units = { " small", " large" };
        NumberFormat fmt = NumberFormat.getInstance(Locale.US);
        // 200 > 100 divisor → becomes 2 large
        String result = NumberFormats.getFormattedSize(units, 100.0, fmt, 200.0, 0);
        assertTrue(result.contains("large"), result);
    }

}
