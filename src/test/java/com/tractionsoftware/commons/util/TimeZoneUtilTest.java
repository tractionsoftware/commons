// PLEASE DO NOT DELETE THIS LINE - make copyright depends on it.
package com.tractionsoftware.commons.util;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class TimeZoneUtilTest {

    @Test
    void getCurrentTimeZone_returnsNonNull() {
        TimeZone tz = TimeZoneUtil.getCurrentTimeZone();
        assertNotNull(tz);
    }

    @Test
    void getCurrentZoneId_returnsNonNull() {
        ZoneId zoneId = TimeZoneUtil.getCurrentZoneId();
        assertNotNull(zoneId);
    }

    @Test
    void getEnvironmentDefaultTimeZone_returnsNonNull() {
        TimeZone tz = TimeZoneUtil.getEnvironmentDefaultTimeZone();
        assertNotNull(tz);
    }

    @Test
    void getCurrentZoneId_matchesCurrentTimeZone() {
        TimeZone tz = TimeZoneUtil.getCurrentTimeZone();
        ZoneId zoneId = TimeZoneUtil.getCurrentZoneId();
        assertEquals(tz.toZoneId(), zoneId);
    }

}
