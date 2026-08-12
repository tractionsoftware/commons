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

public final class SimplestUTCDisplayDateTest {

    private static final Date DATE = new Date(1_000_000_000L);

    // =====================================================================
    // createDefaultInstance
    // =====================================================================

    @Test
    void createDefaultInstance_date_hasCorrectDate() {
        UTCDisplayDate d = SimplestUTCDisplayDate.createDefaultInstance(DATE);
        assertEquals(DATE, d.getDate());
    }

    @Test
    void createDefaultInstance_notAllDay() {
        assertFalse(SimplestUTCDisplayDate.createDefaultInstance(DATE).isAllDay());
    }

    @Test
    void createDefaultInstance_typeIsNone() {
        assertEquals(DateType.NONE, SimplestUTCDisplayDate.createDefaultInstance(DATE).getType());
    }

    @Test
    void createDefaultInstance_long_hasCorrectDate() {
        UTCDisplayDate d = SimplestUTCDisplayDate.createDefaultInstance(12345L);
        assertEquals(new Date(12345L), d.getDate());
    }

    @Test
    void createDefaultInstance_null_throwsNPE() {
        assertThrows(NullPointerException.class, () ->
            SimplestUTCDisplayDate.createDefaultInstance((Date) null));
    }

    // =====================================================================
    // createInstance
    // =====================================================================

    @Test
    void createInstance_notAllDay_sameDate() {
        UTCDisplayDate d = SimplestUTCDisplayDate.createInstance(DateType.NONE, DATE, false);
        assertEquals(DATE, d.getDate());
        assertFalse(d.isAllDay());
    }

    @Test
    void createInstance_allDay_convertsMidnightLocalToGmt() {
        // When allDay=true, the date is converted via DateUtil.cvtMidnightLocalToGmt
        // We just verify it is allDay and has a date
        UTCDisplayDate d = SimplestUTCDisplayDate.createInstance(DateType.NONE, DATE, true);
        assertTrue(d.isAllDay());
        assertNotNull(d.getDate());
    }

    // =====================================================================
    // getWithNewType
    // =====================================================================

    @Test
    void getWithNewType_sameType_returnsSameInstance() {
        UTCDisplayDate original = SimplestUTCDisplayDate.createDefaultInstance(DATE);
        UTCDisplayDate result = SimplestUTCDisplayDate.getWithNewType(original, DateType.NONE);
        assertSame(original, result);
    }

    @Test
    void getWithNewType_differentType_returnsNewInstance() {
        DateType customType = new DateType() {
            @Override public String getName() { return "custom"; }
            @Override public boolean canBeAuthored() { return true; }
        };
        UTCDisplayDate original = SimplestUTCDisplayDate.createDefaultInstance(DATE);
        UTCDisplayDate result = SimplestUTCDisplayDate.getWithNewType(original, customType);
        assertNotSame(original, result);
        assertEquals(customType, result.getType());
    }

    @Test
    void getWithNewType_nullDate_throwsNPE() {
        assertThrows(NullPointerException.class, () ->
            SimplestUTCDisplayDate.getWithNewType(null, DateType.NONE));
    }

    @Test
    void getWithNewType_nullType_throwsNPE() {
        assertThrows(NullPointerException.class, () ->
            SimplestUTCDisplayDate.getWithNewType(SimplestUTCDisplayDate.createDefaultInstance(DATE), null));
    }

    // =====================================================================
    // equals / hashCode
    // =====================================================================

    @Test
    void equals_sameDate_notAllDay_equal() {
        UTCDisplayDate a = SimplestUTCDisplayDate.createDefaultInstance(DATE);
        UTCDisplayDate b = SimplestUTCDisplayDate.createDefaultInstance(DATE);
        assertEquals(a, b);
    }

    @Test
    void equals_differentDate_notEqual() {
        UTCDisplayDate a = SimplestUTCDisplayDate.createDefaultInstance(DATE);
        UTCDisplayDate b = SimplestUTCDisplayDate.createDefaultInstance(new Date(999L));
        assertNotEquals(a, b);
    }

    @Test
    void equals_nonUTCDisplayDate_notEqual() {
        UTCDisplayDate a = SimplestUTCDisplayDate.createDefaultInstance(DATE);
        assertNotEquals(a, "string");
    }

    @Test
    void hashCode_equalObjects_equalHashCode() {
        UTCDisplayDate a = SimplestUTCDisplayDate.createDefaultInstance(DATE);
        UTCDisplayDate b = SimplestUTCDisplayDate.createDefaultInstance(DATE);
        assertEquals(a.hashCode(), b.hashCode());
    }

    // =====================================================================
    // toString
    // =====================================================================

    @Test
    void toString_notNull() {
        assertNotNull(SimplestUTCDisplayDate.createDefaultInstance(DATE).toString());
    }

    @Test
    void toString_allDay_containsAllDay() {
        UTCDisplayDate d = SimplestUTCDisplayDate.createInstance(DateType.NONE, DATE, true);
        assertTrue(d.toString().contains("all day"), d.toString());
    }

}
