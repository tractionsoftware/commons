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

import java.text.NumberFormat;

public final class NumberFormats {

    private NumberFormats() {
    }

    private static final String[] BYTE_SIZE_UNITS = {
        " B", " KB", " MB", " GB", " TB", " PB"
    };

    private static final double BYTE_SIZE_DIVISOR = 1024.0d;

    private static final NumberFormat sizeFormatter() {
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(1);
        return format;
    }

    public static final String getFormattedSize(String[] units, double divisor, NumberFormat formatter, double sz, int unitIndex) {
        if (sz < divisor || (unitIndex == units.length - 1)) {
            // format the size
            return formatter.format(sz) + units[unitIndex];
        }
        return getFormattedSize(units, divisor, formatter, sz / divisor, unitIndex + 1);
    }

    public static final String getFormattedByteSize(double sz) {
        return getFormattedByteSize(sz, 0);
    }

    public static final String getFormattedByteSize(double sz, int unitIndex) {
        return getFormattedSize(BYTE_SIZE_UNITS, BYTE_SIZE_DIVISOR, sizeFormatter(), sz, unitIndex);
    }

    public static final String getFormattedPercentage(double numerator, double divisor) {
        return getFormattedPercentage(numerator, divisor, 0);
    }

    public static final String getFormattedPercentage(double numerator, double divisor, int decimalDigits) {
        try {
            return getFormattedPercentage(numerator / divisor, decimalDigits);
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the given percentage, expressed as a fraction (i.e., with 1 representing 100%) formatted for the current
     * locale.
     *
     * @param percentageAsFraction
     *     the percentage to be formatted, expressed as a fraction of 1 (i.e., with 1 representing 100%).
     * @param decimalDigits
     *     the number of decimal digits to be included in the formatted output.
     * @return the given percentage, expressed as a fraction (i.e., with 1 representing 100%) formatted for the current
     *     locale, if possible; null if the input is not a number, or if there is some other problem formatting.
     */
    public static final String getFormattedPercentage(double percentageAsFraction, int decimalDigits) {
        try {
            if (Double.isInfinite(percentageAsFraction) || Double.isNaN(percentageAsFraction)) {
                return null;
            }
            NumberFormat format = NumberFormat.getPercentInstance();
            if (decimalDigits >= 0) {
                format.setMaximumFractionDigits(decimalDigits);
            }
            return format.format(percentageAsFraction);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static final NumberFormat getDecimalFormat(int fractionDigits) {
        NumberFormat decimalFormat = NumberFormat.getInstance();
        decimalFormat.setMaximumFractionDigits(fractionDigits);
        return decimalFormat;
    }


    public static final String getFormattedWholeNumber(long value) {
        return getWholeNumberFormat().format(value);
    }

    public static final String getFormattedWholeNumber(double value) {
        return getWholeNumberFormat().format(value);
    }

    public static final NumberFormat getWholeNumberFormat() {
        return NumberFormat.getIntegerInstance();
    }

}
