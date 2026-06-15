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

import java.time.ZoneId;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

public final class TimeZoneUtilTest {

    @Test
    void getCurrentTimeZone_returnsNonNull() {
        assertNotNull(TimeZoneUtil.getCurrentTimeZone());
    }

    @Test
    void getCurrentTimeZone_isTimeZone() {
        assertInstanceOf(TimeZone.class, TimeZoneUtil.getCurrentTimeZone());
    }

    @Test
    void getCurrentZoneId_returnsNonNull() {
        assertNotNull(TimeZoneUtil.getCurrentZoneId());
    }

    @Test
    void getCurrentZoneId_isZoneId() {
        assertInstanceOf(ZoneId.class, TimeZoneUtil.getCurrentZoneId());
    }

    @Test
    void getEnvironmentDefaultTimeZone_returnsNonNull() {
        assertNotNull(TimeZoneUtil.getEnvironmentDefaultTimeZone());
    }

    @Test
    void getCurrentZoneId_matchesCurrentTimeZone() {
        TimeZone tz = TimeZoneUtil.getCurrentTimeZone();
        ZoneId zoneId = TimeZoneUtil.getCurrentZoneId();
        assertEquals(tz.toZoneId(), zoneId);
    }

}
