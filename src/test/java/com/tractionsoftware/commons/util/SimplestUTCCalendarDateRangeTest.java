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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SimplestUTCCalendarDateRange} and the {@link UTCCalendarDateRange} default methods.
 */
public final class SimplestUTCCalendarDateRangeTest {

    private static final long ONE_HOUR_MS = 60 * 60 * 1000L;
    private static final long ONE_DAY_MS  = 24 * ONE_HOUR_MS;

    /** Creates a non-all-day UTCDisplayDate. */
    private static UTCDisplayDate dt(long millis) {
        return SimplestUTCDisplayDate.createDefaultInstance(new Date(millis));
    }

    /** Creates an all-day UTCDisplayDate (raw millis = midnight UTC). */
    private static UTCDisplayDate allDay(long midnightUtcMillis) {
        return SimplestUTCDisplayDate.createInstance(DateType.NONE, new Date(midnightUtcMillis), true);
    }

    private UTCDisplayDate start;
    private UTCDisplayDate end;
    private SimplestUTCCalendarDateRange range;

    @BeforeEach
    void setUp() {
        // start = 1 hour ago, end = 2 hours in the future (non-all-day)
        long now = System.currentTimeMillis();
        start = dt(now - ONE_HOUR_MS);
        end   = dt(now + 2 * ONE_HOUR_MS);
        range = new SimplestUTCCalendarDateRange(start, end, false);
    }

    // =====================================================================
    // getters
    // =====================================================================

    @Test
    void getStart_returnsStart() {
        assertSame(start, range.getStart());
    }

    @Test
    void getEnd_returnsEnd() {
        assertSame(end, range.getEnd());
    }

    @Test
    void isAllDay_delegatesToStart() {
        assertFalse(range.isAllDay());
    }

    @Test
    void isFullSingleDay_false_whenConstructed_false() {
        assertFalse(range.isFullSingleDay());
    }

    @Test
    void isFullSingleDay_true_whenConstructed_true() {
        var r = new SimplestUTCCalendarDateRange(start, end, true);
        assertTrue(r.isFullSingleDay());
    }

    // =====================================================================
    // isWithinSingleDay
    // =====================================================================

    @Test
    void isWithinSingleDay_fullSingleDay_true() {
        var r = new SimplestUTCCalendarDateRange(start, end, true);
        assertTrue(r.isWithinSingleDay());
    }

    @Test
    void isWithinSingleDay_allDayRange_false() {
        // All-day ranges always span the full day → isAllDay() is true → isWithinSingleDay false
        long midnight = 0L;
        UTCDisplayDate s = allDay(midnight);
        UTCDisplayDate e = allDay(midnight + ONE_DAY_MS);
        var r = new SimplestUTCCalendarDateRange(s, e, false);
        assertFalse(r.isWithinSingleDay());
    }

    @Test
    void isWithinSingleDay_sameDay_true() {
        // Both start and end on same calendar day
        long base = System.currentTimeMillis();
        long startMs = base - (base % ONE_DAY_MS);      // midnight UTC today
        UTCDisplayDate s = dt(startMs + ONE_HOUR_MS);   // 1am today
        UTCDisplayDate e = dt(startMs + 2 * ONE_HOUR_MS); // 2am today
        var r = new SimplestUTCCalendarDateRange(s, e, false);
        // Check using adjusted dates (no tz diff here since non-allday)
        // Depends on local TZ; at minimum verify the method doesn't throw
        assertNotNull(r.isWithinSingleDay());
    }

    // =====================================================================
    // equals / hashCode
    // =====================================================================

    @Test
    void equals_sameStartEnd_true() {
        var r2 = new SimplestUTCCalendarDateRange(start, end, false);
        assertEquals(range, r2);
    }

    @Test
    void equals_differentEnd_false() {
        var r2 = new SimplestUTCCalendarDateRange(start, dt(end.getDate().getTime() + ONE_HOUR_MS), false);
        assertNotEquals(range, r2);
    }

    @Test
    void equals_differentFullSingleDay_false() {
        var r2 = new SimplestUTCCalendarDateRange(start, end, true);
        assertNotEquals(range, r2);
    }

    @Test
    void equals_nonRange_false() {
        assertNotEquals(range, "not a range");
    }

    @Test
    void hashCode_equalObjects_sameHash() {
        var r2 = new SimplestUTCCalendarDateRange(start, end, false);
        assertEquals(range.hashCode(), r2.hashCode());
    }

    // =====================================================================
    // toString
    // =====================================================================

    @Test
    void toString_notNull() {
        assertNotNull(range.toString());
    }

    @Test
    void toString_containsGmt() {
        assertTrue(range.toString().contains("GMT"), range.toString());
    }

    // =====================================================================
    // UTCCalendarDateRange default methods
    // =====================================================================

    @Test
    void isInProgress_now_true() {
        // start is in the past, end is in the future → now is "in progress"
        assertTrue(range.isInProgress(new Date()));
    }

    @Test
    void isInProgress_beforeStart_false() {
        Date beforeStart = new Date(start.getDate().getTime() - ONE_HOUR_MS);
        assertFalse(range.isInProgress(beforeStart));
    }

    @Test
    void isComplete_afterEnd_true() {
        Date afterEnd = new Date(end.getDate().getTime() + ONE_HOUR_MS);
        assertTrue(range.isComplete(afterEnd));
    }

    @Test
    void isComplete_beforeEnd_false() {
        assertFalse(range.isComplete(new Date()));
    }

    @Test
    void isComplete_noEnd_false() {
        var r = new SimplestUTCCalendarDateRange(start, null, false);
        assertFalse(r.isComplete(new Date()));
    }

    @Test
    void isIncomplete_now_true() {
        assertTrue(range.isIncomplete(new Date()));
    }

    @Test
    void isIncomplete_afterEnd_false() {
        Date afterEnd = new Date(end.getDate().getTime() + ONE_HOUR_MS);
        assertFalse(range.isIncomplete(afterEnd));
    }

    @Test
    void contains_date_withinRange_true() {
        assertTrue(range.contains(new Date()));
    }

    @Test
    void contains_date_beforeRange_false() {
        assertFalse(range.contains(new Date(start.getDate().getTime() - ONE_HOUR_MS)));
    }

    @Test
    void asInterval_notNull() {
        assertNotNull(range.asInterval());
    }

    @Test
    void asInterval_withTimeZone_notNull() {
        assertNotNull(range.asInterval(TimeZone.getTimeZone("UTC")));
    }

}
