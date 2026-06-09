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

    @Test
    void getDateFormat_producesSimpleDateFormat() {
        SimpleDateFormat fmt = DateFormats.getDateFormat("yyyy-MM-dd");
        assertNotNull(fmt);
        // Should format a specific date in a recognizable way
        Calendar cal = new GregorianCalendar(DateUtil.GMT);
        cal.set(2024, Calendar.MARCH, 15, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        fmt.setTimeZone(DateUtil.GMT);
        assertEquals("2024-03-15", fmt.format(cal.getTime()));
    }

    @Test
    void getDateFormat_differentPatternsProduceDifferentResults() {
        Date d = new Date(0); // epoch
        SimpleDateFormat fmtA = DateFormats.getDateFormat("yyyy");
        SimpleDateFormat fmtB = DateFormats.getDateFormat("MM");
        fmtA.setTimeZone(DateUtil.GMT);
        fmtB.setTimeZone(DateUtil.GMT);
        assertNotEquals(fmtA.format(d), fmtB.format(d));
    }

    @Test
    void getHttpDateFormat_usesGmt() {
        SimpleDateFormat fmt = DateFormats.getHttpDateFormat();
        assertNotNull(fmt);
        assertEquals("GMT", fmt.getTimeZone().getID());
    }

    @Test
    void getHttpDateFormat_withTimeZone_usesGivenZone() {
        TimeZone pst = TimeZone.getTimeZone("America/Los_Angeles");
        SimpleDateFormat fmt = DateFormats.getHttpDateFormat(pst);
        assertNotNull(fmt);
        assertEquals("America/Los_Angeles", fmt.getTimeZone().getID());
    }

    @Test
    void getUrlDateFormat_noArg_returnsNonNull() {
        assertNotNull(DateFormats.getUrlDateFormat());
    }

    @Test
    void getUrlDateFormat_withTimeZone_usesGivenZone() {
        TimeZone tz = TimeZone.getTimeZone("Asia/Tokyo");
        SimpleDateFormat fmt = DateFormats.getUrlDateFormat(tz);
        assertNotNull(fmt);
        assertEquals("Asia/Tokyo", fmt.getTimeZone().getID());
    }

    @Test
    void getUrlDateTimeFormat_returnsNonNull() {
        assertNotNull(DateFormats.getUrlDateTimeFormat());
    }

    @Test
    void getDefaultDateFormat_returnsNonNull() {
        assertNotNull(DateFormats.getDefaultDateFormat("yyyy-MM-dd"));
    }

    @Test
    void getTimeStampFormat_formatsWithExpectedPattern() {
        SimpleDateFormat fmt = DateFormats.getTimeStampFormat();
        assertNotNull(fmt);
        // Result should match yyyy-MM-dd HH:mm:ss pattern (19 chars like "2024-01-01 00:00:00")
        String result = fmt.format(new Date(0));
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"), result);
    }

    @Test
    void getTimeStampFormatWithTimeZone_includesTimeZone() {
        SimpleDateFormat fmt = DateFormats.getTimeStampFormatWithTimeZone();
        assertNotNull(fmt);
        String result = fmt.format(new Date(0));
        // Should have at least 20 chars (timestamp + space + timezone abbr)
        assertTrue(result.length() > 19, result);
    }

    @Test
    void getExlfDateFormat_usesGmt() {
        SimpleDateFormat fmt = DateFormats.getExlfDateFormat();
        assertNotNull(fmt);
        assertEquals("GMT", fmt.getTimeZone().getID());
    }

    @Test
    void getExlfTimeFormat_usesGmt() {
        SimpleDateFormat fmt = DateFormats.getExlfTimeFormat();
        assertNotNull(fmt);
        assertEquals("GMT", fmt.getTimeZone().getID());
    }

    @Test
    void getGregorianCalendar_noArgs_returnsNonNull() {
        GregorianCalendar cal = DateFormats.getGregorianCalendar();
        assertNotNull(cal);
    }

    @Test
    void getGregorianCalendar_withTimeZone_usesGivenZone() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        GregorianCalendar cal = DateFormats.getGregorianCalendar(tz);
        assertNotNull(cal);
        assertEquals("America/New_York", cal.getTimeZone().getID());
    }

    @Test
    void getGregorianCalendar_withDate_setsDate() {
        Date d = new Date(123456789L);
        GregorianCalendar cal = DateFormats.getGregorianCalendar(DateUtil.GMT, d);
        assertNotNull(cal);
        assertEquals(d, cal.getTime());
    }

    @Test
    void setToMidnight_resetsTimeFields() {
        GregorianCalendar cal = new GregorianCalendar(DateUtil.GMT);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 45);
        cal.set(Calendar.MILLISECOND, 123);
        DateFormats.setToMidnight(cal);
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test
    void getTimeFormatStyle_blank_returnsDefault() {
        assertEquals(DateFormat.SHORT, DateFormats.getTimeFormatStyle("", DateFormat.SHORT));
        assertEquals(DateFormat.MEDIUM, DateFormats.getTimeFormatStyle(null, DateFormat.MEDIUM));
    }

    @Test
    void getTimeFormatStyle_knownValues() {
        assertEquals(DateFormat.SHORT, DateFormats.getTimeFormatStyle(DateFormats.DATEFORMAT_SHORT, DateFormat.FULL));
        assertEquals(DateFormat.SHORT, DateFormats.getTimeFormatStyle(DateFormats.DATEFORMAT_TINY, DateFormat.FULL));
        assertEquals(DateFormat.MEDIUM, DateFormats.getTimeFormatStyle(DateFormats.DATEFORMAT_MEDIUM, DateFormat.FULL));
        assertEquals(DateFormat.LONG, DateFormats.getTimeFormatStyle(DateFormats.DATEFORMAT_LONG, DateFormat.FULL));
        assertEquals(DateFormat.FULL, DateFormats.getTimeFormatStyle(DateFormats.DATEFORMAT_FULL, DateFormat.SHORT));
        assertEquals(DateFormats.DATEFORMAT_STYLE_RAW, DateFormats.getTimeFormatStyle(DateFormats.DATEFORMAT_RAW, DateFormat.SHORT));
    }

    @Test
    void getDateInstance_returnsNonNull() {
        assertNotNull(DateFormats.getDateInstance(DateFormat.SHORT));
    }

    @Test
    void getTimeInstance_returnsNonNull() {
        assertNotNull(DateFormats.getTimeInstance(DateFormat.SHORT));
    }

    @Test
    void getDateTimeInstance_returnsNonNull() {
        assertNotNull(DateFormats.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT));
    }

    @Test
    void getLogFileDateFormat_returnsNonNull() {
        assertNotNull(DateFormats.getLogFileDateFormat());
    }

    @Test
    void getCommentDateFormat_returnsNonNull() {
        assertNotNull(DateFormats.getCommentDateFormat());
    }

    @Test
    void getCalendarDialogFormat_returnsNonNull() {
        assertNotNull(DateFormats.getCalendarDialogFormat());
    }

    @Test
    void getRsDisplayFormat_returnsNonNull() {
        assertNotNull(DateFormats.getRsDisplayFormat());
    }

    @Test
    void getRsDateFormat_returnsNonNull() {
        assertNotNull(DateFormats.getRsDateFormat());
    }

    @Test
    void getServerTimeZoneFormat_returnsNonNull() {
        assertNotNull(DateFormats.getServerTimeZoneFormat());
    }

    @Test
    void getDefaultArchiveLogFileNameDateStampFormat_returnsNonNull() {
        assertNotNull(DateFormats.getDefaultArchiveLogFileNameDateStampFormat());
    }

    @Test
    void getCookieDateFormat_returnsNonNull() {
        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
        assertNotNull(DateFormats.getCookieDateFormat(tz));
    }

    @Test
    void getGregorianCalendarForDay_setsToMidnight() {
        Date d = new Date(123456789L);
        GregorianCalendar cal = DateFormats.getGregorianCalendarForDay(DateUtil.GMT, d);
        assertNotNull(cal);
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
    }

    @Test
    void getTimeToday_sameDay_returnsTime() {
        // Just confirm it returns a non-null, non-empty string
        Date now = new Date();
        String result = DateFormats.getTimeToday(now, now, DateFormat.SHORT, DateFormat.SHORT);
        assertNotNull(result);
        assertFalse(result.isBlank());
    }

}
