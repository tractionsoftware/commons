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

package com.tractionsoftware.commons.util;

import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

public final class DateFormatsTest {

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    // =====================================================================
    // getDateFormat(String pattern)
    // =====================================================================

    @Test
    void getDateFormat_pattern_returnsSimpleDateFormat() {
        SimpleDateFormat fmt = DateFormats.getDateFormat("yyyy-MM-dd");
        assertNotNull(fmt);
    }

    @Test
    void getDateFormat_pattern_formatsCorrectly() throws Exception {
        SimpleDateFormat fmt = DateFormats.getDateFormat("yyyy");
        fmt.setTimeZone(GMT);
        // epoch year is 1970
        assertEquals("1970", fmt.format(new Date(0)));
    }

    // =====================================================================
    // getHttpDateFormat
    // =====================================================================

    @Test
    void getHttpDateFormat_returnsNotNull() {
        assertNotNull(DateFormats.getHttpDateFormat());
    }

    @Test
    void getHttpDateFormat_usesGMT() {
        SimpleDateFormat fmt = DateFormats.getHttpDateFormat();
        assertEquals(GMT.getID(), fmt.getTimeZone().getID());
    }

    @Test
    void getHttpDateFormat_withTimeZone_usesProvidedZone() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        SimpleDateFormat fmt = DateFormats.getHttpDateFormat(tz);
        assertEquals(tz.getID(), fmt.getTimeZone().getID());
    }

    @Test
    void getHttpDateFormat_epoch_containsJan() {
        SimpleDateFormat fmt = DateFormats.getHttpDateFormat();
        String formatted = fmt.format(new Date(0));
        assertTrue(formatted.contains("Jan"), formatted);
        assertTrue(formatted.contains("1970"), formatted);
    }

    // =====================================================================
    // getUrlDateFormat
    // =====================================================================

    @Test
    void getUrlDateFormat_returnsNotNull() {
        assertNotNull(DateFormats.getUrlDateFormat());
    }

    @Test
    void getUrlDateFormat_epoch_isYYYYMMDD() {
        SimpleDateFormat fmt = DateFormats.getUrlDateFormat(GMT);
        assertEquals("19700101", fmt.format(new Date(0)));
    }

    // =====================================================================
    // getUrlDateTimeFormat
    // =====================================================================

    @Test
    void getUrlDateTimeFormat_returnsNotNull() {
        assertNotNull(DateFormats.getUrlDateTimeFormat());
    }

    // =====================================================================
    // getTimeStampFormat
    // =====================================================================

    @Test
    void getTimeStampFormat_returnsNotNull() {
        assertNotNull(DateFormats.getTimeStampFormat());
    }

    @Test
    void getTimeStampFormat_epoch_inGMT_hasExpectedFormat() throws Exception {
        SimpleDateFormat fmt = DateFormats.getTimeStampFormat();
        fmt.setTimeZone(GMT);
        assertEquals("1970-01-01 00:00:00", fmt.format(new Date(0)));
    }

    // =====================================================================
    // getTimeStampFormatWithTimeZone
    // =====================================================================

    @Test
    void getTimeStampFormatWithTimeZone_returnsNotNull() {
        assertNotNull(DateFormats.getTimeStampFormatWithTimeZone());
    }

    // =====================================================================
    // getExlfDateFormat / getExlfTimeFormat
    // =====================================================================

    @Test
    void getExlfDateFormat_epoch_is1970_01_01() {
        SimpleDateFormat fmt = DateFormats.getExlfDateFormat();
        assertEquals("1970-01-01", fmt.format(new Date(0)));
    }

    @Test
    void getExlfTimeFormat_epoch_is00_00_00() {
        SimpleDateFormat fmt = DateFormats.getExlfTimeFormat();
        assertEquals("00:00:00", fmt.format(new Date(0)));
    }

    // =====================================================================
    // getIcalDatestampFormats (constants)
    // =====================================================================

    @Test
    void icalDatestampAllDay_hasNoTime() {
        assertFalse(DateFormats.DATEFORMAT_ICAL_DATESTAMP_ALLDAY.contains("'T'"));
    }

    @Test
    void icalDatestamp_hasTimeComponent() {
        assertTrue(DateFormats.DATEFORMAT_ICAL_DATESTAMP.contains("'T'"));
    }

    // =====================================================================
    // setToMidnight
    // =====================================================================

    @Test
    void setToMidnight_clearsTimeFields() {
        GregorianCalendar cal = new GregorianCalendar(GMT);
        cal.set(2024, Calendar.MARCH, 15, 14, 30, 45);
        cal.set(Calendar.MILLISECOND, 123);
        DateFormats.setToMidnight(cal);

        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test
    void setToMidnight_preservesDateFields() {
        GregorianCalendar cal = new GregorianCalendar(GMT);
        cal.set(2024, Calendar.JUNE, 7, 12, 0, 0);
        DateFormats.setToMidnight(cal);

        assertEquals(2024, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JUNE, cal.get(Calendar.MONTH));
        assertEquals(7, cal.get(Calendar.DAY_OF_MONTH));
    }

    // =====================================================================
    // getGregorianCalendar
    // =====================================================================

    @Test
    void getGregorianCalendar_timeZone_usesTimeZone() {
        GregorianCalendar cal = DateFormats.getGregorianCalendar(GMT);
        assertNotNull(cal);
        assertEquals(GMT.getID(), cal.getTimeZone().getID());
    }

    @Test
    void getGregorianCalendar_nullTimeZone_usesDefault() {
        assertNotNull(DateFormats.getGregorianCalendar((TimeZone) null));
    }

    @Test
    void getGregorianCalendar_date_isSetToDate() {
        Date d = new Date(123456789L);
        GregorianCalendar cal = DateFormats.getGregorianCalendar(GMT, d);
        assertEquals(d, cal.getTime());
    }

    // =====================================================================
    // getGregorianCalendarForDay
    // =====================================================================

    @Test
    void getGregorianCalendarForDay_isAtMidnight() {
        Date d = new Date(0L); // epoch
        GregorianCalendar cal = DateFormats.getGregorianCalendarForDay(GMT, d);
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    // =====================================================================
    // getTimeFormatStyle
    // =====================================================================

    @Test
    void getTimeFormatStyle_short_returnsDateFormatShort() {
        assertEquals(DateFormat.SHORT, DateFormats.getTimeFormatStyle("short", DateFormat.MEDIUM));
    }

    @Test
    void getTimeFormatStyle_medium_returnsDateFormatMedium() {
        assertEquals(DateFormat.MEDIUM, DateFormats.getTimeFormatStyle("medium", DateFormat.SHORT));
    }

    @Test
    void getTimeFormatStyle_long_returnsDateFormatLong() {
        assertEquals(DateFormat.LONG, DateFormats.getTimeFormatStyle("long", DateFormat.SHORT));
    }

    @Test
    void getTimeFormatStyle_full_returnsDateFormatFull() {
        assertEquals(DateFormat.FULL, DateFormats.getTimeFormatStyle("full", DateFormat.SHORT));
    }

    @Test
    void getTimeFormatStyle_tiny_returnsShort() {
        assertEquals(DateFormat.SHORT, DateFormats.getTimeFormatStyle("tiny", DateFormat.MEDIUM));
    }

    @Test
    void getTimeFormatStyle_raw_returnsStyleRaw() {
        assertEquals(DateFormats.DATEFORMAT_STYLE_RAW, DateFormats.getTimeFormatStyle("raw", DateFormat.SHORT));
    }

    @Test
    void getTimeFormatStyle_blank_returnsDefault() {
        assertEquals(DateFormat.LONG, DateFormats.getTimeFormatStyle("", DateFormat.LONG));
    }

    @Test
    void getTimeFormatStyle_null_returnsDefault() {
        assertEquals(DateFormat.MEDIUM, DateFormats.getTimeFormatStyle(null, DateFormat.MEDIUM));
    }

    @Test
    void getTimeFormatStyle_unknown_returnsDefault() {
        assertEquals(DateFormat.SHORT, DateFormats.getTimeFormatStyle("unknown", DateFormat.SHORT));
    }

    // =====================================================================
    // getTimeToday
    // =====================================================================

    @Test
    void getTimeToday_sameDay_returnsTime() {
        Date today = new Date(0L);
        Date same = new Date(0L);
        String result = DateFormats.getTimeToday(same, today, DateFormat.SHORT, DateFormat.SHORT);
        assertNotNull(result);
    }

    @Test
    void getTimeToday_differentDay_returnsDate() {
        // today = epoch, date = 2 days later
        Date today = new Date(0L);
        Date other = new Date(2 * 24 * 60 * 60 * 1000L);
        String result = DateFormats.getTimeToday(other, today, DateFormat.SHORT, DateFormat.SHORT);
        assertNotNull(result);
    }

    // =====================================================================
    // getDateFormat(int dateStyle, int timeStyle)
    // =====================================================================

    @Test
    void getDateFormat_dateOnly_returnsNotNull() {
        assertNotNull(DateFormats.getDateFormat(DateFormat.SHORT, DateFormats.DATEFORMAT_STYLE_NONE));
    }

    @Test
    void getDateFormat_timeOnly_returnsNotNull() {
        assertNotNull(DateFormats.getDateFormat(DateFormats.DATEFORMAT_STYLE_NONE, DateFormat.SHORT));
    }

    @Test
    void getDateFormat_dateAndTime_returnsNotNull() {
        assertNotNull(DateFormats.getDateFormat(DateFormat.SHORT, DateFormat.SHORT));
    }

    // =====================================================================
    // constants
    // =====================================================================

    @Test
    void dateFormatStyleNone_isMinusOne() {
        assertEquals(-1, DateFormats.DATEFORMAT_STYLE_NONE);
    }

    @Test
    void dateFormatStyleRaw_isMinusOneThousand() {
        assertEquals(-1000, DateFormats.DATEFORMAT_STYLE_RAW);
    }

}
