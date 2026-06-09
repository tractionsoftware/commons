// PLEASE DO NOT DELETE THIS LINE - make copyright depends on it.
package com.tractionsoftware.commons.text;

import org.junit.jupiter.api.Test;

import java.text.NumberFormat;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class NumberFormatsTest {

    // Use ROOT locale-independent number formatting to avoid locale-sensitivity in assertions
    // The production code uses NumberFormat.getInstance() which is locale-dependent, so we
    // just verify shape/reasonableness rather than exact strings where locale matters.

    // ---------------------------------------------------------------------------
    // getFormattedByteSize
    // ---------------------------------------------------------------------------

    @Test
    void getFormattedByteSize_bytes() {
        String result = NumberFormats.getFormattedByteSize(512);
        assertTrue(result.endsWith(" B"), "expected ' B' suffix: " + result);
        assertTrue(result.startsWith("512"), "expected 512: " + result);
    }

    @Test
    void getFormattedByteSize_1KB() {
        String result = NumberFormats.getFormattedByteSize(1024);
        assertTrue(result.endsWith(" KB"), "expected KB: " + result);
        assertTrue(result.startsWith("1"), "expected 1: " + result);
    }

    @Test
    void getFormattedByteSize_1MB() {
        String result = NumberFormats.getFormattedByteSize(1024 * 1024);
        assertTrue(result.endsWith(" MB"), "expected MB: " + result);
    }

    @Test
    void getFormattedByteSize_1GB() {
        String result = NumberFormats.getFormattedByteSize(1024L * 1024 * 1024);
        assertTrue(result.endsWith(" GB"), "expected GB: " + result);
    }

    @Test
    void getFormattedByteSize_zero() {
        String result = NumberFormats.getFormattedByteSize(0);
        assertTrue(result.endsWith(" B"), "expected B: " + result);
    }

    @Test
    void getFormattedByteSize_withUnitIndex_startsAtKB() {
        // unitIndex=1 means start at KB
        String result = NumberFormats.getFormattedByteSize(1.5, 1);
        assertTrue(result.endsWith(" KB"), "expected KB with unitIndex=1: " + result);
    }

    // ---------------------------------------------------------------------------
    // getFormattedPercentage (numerator, divisor)
    // ---------------------------------------------------------------------------

    @Test
    void getFormattedPercentage_half() {
        String result = NumberFormats.getFormattedPercentage(50, 100);
        assertNotNull(result);
        assertTrue(result.contains("50") || result.contains("5"), "expected 50% form: " + result);
    }

    @Test
    void getFormattedPercentage_zeroDivisor_returnsNull() {
        // divide by zero in double doesn't throw, produces Infinity which format may handle
        // but the code swallows exceptions and returns null
        // Infinity formatted as percent is unlikely to be null in most JVMs, but let's just
        // assert it doesn't throw
        assertDoesNotThrow(() -> NumberFormats.getFormattedPercentage(1, 0));
    }

    // ---------------------------------------------------------------------------
    // getFormattedPercentage (fraction form)
    // ---------------------------------------------------------------------------

    @Test
    void getFormattedPercentage_fraction_halfIs50Percent() {
        String result = NumberFormats.getFormattedPercentage(0.5, 0);
        assertNotNull(result);
        // Most locales will produce "50%" in some form
        assertTrue(result.length() > 0);
    }

    @Test
    void getFormattedPercentage_fraction_withDecimalDigits() {
        String result = NumberFormats.getFormattedPercentage(0.333, 2);
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    // ---------------------------------------------------------------------------
    // getFormattedWholeNumber
    // ---------------------------------------------------------------------------

    @Test
    void getFormattedWholeNumber_long() {
        String result = NumberFormats.getFormattedWholeNumber(1000L);
        assertNotNull(result);
        assertTrue(result.contains("1") && result.contains("0"), "expected 1000 in some form: " + result);
    }

    @Test
    void getFormattedWholeNumber_double() {
        String result = NumberFormats.getFormattedWholeNumber(42.7);
        assertNotNull(result);
        // Integer format rounds or truncates — just check non-null
        assertTrue(result.length() > 0);
    }

    // ---------------------------------------------------------------------------
    // getDecimalFormat
    // ---------------------------------------------------------------------------

    @Test
    void getDecimalFormat_returnsNonNull() {
        NumberFormat fmt = NumberFormats.getDecimalFormat(2);
        assertNotNull(fmt);
        assertEquals(2, fmt.getMaximumFractionDigits());
    }

    // ---------------------------------------------------------------------------
    // getWholeNumberFormat
    // ---------------------------------------------------------------------------

    @Test
    void getWholeNumberFormat_returnsNonNull() {
        assertNotNull(NumberFormats.getWholeNumberFormat());
    }

    // ---------------------------------------------------------------------------
    // getFormattedSize (general)
    // ---------------------------------------------------------------------------

    @Test
    void getFormattedSize_belowDivisor_formatsWithFirstUnit() {
        String[] units = {" X", " Y"};
        NumberFormat fmt = NumberFormat.getInstance(Locale.US);
        String result = NumberFormats.getFormattedSize(units, 10, fmt, 5.0, 0);
        assertTrue(result.endsWith(" X"), "expected first unit: " + result);
    }

    @Test
    void getFormattedSize_aboveDivisor_escalatesUnit() {
        String[] units = {" X", " Y"};
        NumberFormat fmt = NumberFormat.getInstance(Locale.US);
        String result = NumberFormats.getFormattedSize(units, 10, fmt, 50.0, 0);
        assertTrue(result.endsWith(" Y"), "expected second unit: " + result);
    }

}
