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

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link UTCDisplayDate} default interface methods, exercised via {@link SimplestUTCDisplayDate}.
 */
public final class UTCDisplayDateTest {

    private static UTCDisplayDate date(long millis) {
        return SimplestUTCDisplayDate.createDefaultInstance(new Date(millis));
    }

    // =====================================================================
    // getAdjustedDate (default method)
    // =====================================================================

    @Test
    void getAdjustedDate_notAllDay_returnsSameDate() {
        Date d = new Date(1_000_000L);
        UTCDisplayDate utc = SimplestUTCDisplayDate.createDefaultInstance(d);
        // not all-day → no adjustment
        assertEquals(d, utc.getAdjustedDate());
    }

    @Test
    void getAdjustedDate_allDay_returnsMidnightLocal() {
        // All-day dates are adjusted from UTC midnight to local midnight.
        // We only check it is non-null and a Date.
        UTCDisplayDate utc = SimplestUTCDisplayDate.createInstance(DateType.NONE, new Date(0), true);
        assertNotNull(utc.getAdjustedDate());
    }

    // =====================================================================
    // getAdjustedCalendar (default method)
    // =====================================================================

    @Test
    void getAdjustedCalendar_notNull() {
        UTCDisplayDate utc = date(0);
        assertNotNull(utc.getAdjustedCalendar());
    }

    // =====================================================================
    // isOnSameDay (default method)
    // =====================================================================

    @Test
    void isOnSameDay_sameInstant_true() {
        long ms = System.currentTimeMillis();
        UTCDisplayDate a = date(ms);
        UTCDisplayDate b = date(ms);
        assertTrue(a.isOnSameDay(b));
    }

    @Test
    void isOnSameDay_null_false() {
        UTCDisplayDate a = date(0);
        assertFalse(a.isOnSameDay(null));
    }

    @Test
    void isOnSameDay_farApart_false() {
        UTCDisplayDate a = date(0);
        // 2 days later
        UTCDisplayDate b = date(2 * 24 * 60 * 60 * 1000L);
        assertFalse(a.isOnSameDay(b));
    }

    // =====================================================================
    // compareTo (default method)
    // =====================================================================

    @Test
    void compareTo_earlier_negative() {
        UTCDisplayDate a = date(1000);
        UTCDisplayDate b = date(2000);
        assertTrue(a.compareTo(b) < 0);
    }

    @Test
    void compareTo_later_positive() {
        UTCDisplayDate a = date(2000);
        UTCDisplayDate b = date(1000);
        assertTrue(a.compareTo(b) > 0);
    }

    @Test
    void compareTo_same_zero() {
        UTCDisplayDate a = date(5000);
        UTCDisplayDate b = date(5000);
        assertEquals(0, a.compareTo(b));
    }

}
