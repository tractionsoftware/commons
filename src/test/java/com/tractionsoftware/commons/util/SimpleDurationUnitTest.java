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

import com.tractionsoftware.commons.properties.MapPropertyStore;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public final class SimpleDurationUnitTest {

    // =====================================================================
    // defaultParseValue(String)
    // =====================================================================

    @Test
    void defaultParseValue_null_returnsNull() {
        assertNull(SimpleDurationUnit.defaultParseValue(null));
    }

    @Test
    void defaultParseValue_blank_returnsNull() {
        assertNull(SimpleDurationUnit.defaultParseValue("  "));
    }

    @Test
    void defaultParseValue_zero_returnsDurationZero() {
        assertEquals(Duration.ZERO, SimpleDurationUnit.defaultParseValue("0"));
    }

    @Test
    void defaultParseValue_iso8601_parsed() {
        // ISO-8601 format: PT5M = 5 minutes
        assertEquals(Duration.ofMinutes(5), SimpleDurationUnit.defaultParseValue("PT5M"));
    }

    @Test
    void defaultParseValue_days() {
        assertEquals(Duration.ofDays(2), SimpleDurationUnit.defaultParseValue("2d"));
    }

    @Test
    void defaultParseValue_hours() {
        assertEquals(Duration.ofHours(3), SimpleDurationUnit.defaultParseValue("3h"));
    }

    @Test
    void defaultParseValue_minutes() {
        assertEquals(Duration.ofMinutes(15), SimpleDurationUnit.defaultParseValue("15m"));
    }

    @Test
    void defaultParseValue_seconds() {
        assertEquals(Duration.ofSeconds(30), SimpleDurationUnit.defaultParseValue("30s"));
    }

    @Test
    void defaultParseValue_milliseconds() {
        assertEquals(Duration.ofMillis(500), SimpleDurationUnit.defaultParseValue("500ms"));
    }

    @Test
    void defaultParseValue_composite_daysHoursMinutes() {
        Duration expected = Duration.ofDays(1).plusHours(2).plusMinutes(30);
        assertEquals(expected, SimpleDurationUnit.defaultParseValue("1d 2h 30m"));
    }

    @Test
    void defaultParseValue_unrecognized_returnsDefaultValue() {
        Duration def = Duration.ofSeconds(10);
        assertEquals(def, SimpleDurationUnit.defaultParseValue("not-a-duration", def));
    }

    @Test
    void defaultParseValue_withDefault_usesDefaultWhenBlank() {
        Duration def = Duration.ofHours(1);
        assertEquals(def, SimpleDurationUnit.defaultParseValue(null, def));
    }

    // =====================================================================
    // enum constants
    // =====================================================================

    @Test
    void getUnit_days() {
        assertEquals(TimeUnit.DAYS, SimpleDurationUnit.DAYS.getUnit());
    }

    @Test
    void getUnit_hours() {
        assertEquals(TimeUnit.HOURS, SimpleDurationUnit.HOURS.getUnit());
    }

    @Test
    void getUnit_milliseconds() {
        assertEquals(TimeUnit.MILLISECONDS, SimpleDurationUnit.MILLISECONDS.getUnit());
    }

    @Test
    void getDuration_days_correct() {
        assertEquals(Duration.ofDays(3), SimpleDurationUnit.DAYS.getDuration(3));
    }

    @Test
    void getDuration_hours_correct() {
        assertEquals(Duration.ofHours(12), SimpleDurationUnit.HOURS.getDuration(12));
    }

    @Test
    void getDuration_milliseconds_correct() {
        assertEquals(Duration.ofMillis(250), SimpleDurationUnit.MILLISECONDS.getDuration(250));
    }

    // =====================================================================
    // loadValueMillis / loadValue
    // =====================================================================

    @Test
    void loadValueMillis_propertyPresent_returnsMillis() {
        MapPropertyStore<Void> props = new MapPropertyStore<>();
        props.putProperty("h", "2");  // "h" is the prop name for HOURS
        assertEquals(TimeUnit.HOURS.toMillis(2), SimpleDurationUnit.HOURS.loadValueMillis(props));
    }

    @Test
    void loadValueMillis_propertyAbsent_returnsZero() {
        MapPropertyStore<Void> props = new MapPropertyStore<>();
        assertEquals(0, SimpleDurationUnit.HOURS.loadValueMillis(props));
    }

    @Test
    void loadValue_propertyPresent_returnsDuration() {
        MapPropertyStore<Void> props = new MapPropertyStore<>();
        props.putProperty("s", "45");  // "s" for SECONDS
        assertEquals(Duration.ofSeconds(45), SimpleDurationUnit.SECONDS.loadValue(props));
    }

    @Test
    void loadValue_propertyAbsent_returnsNull() {
        MapPropertyStore<Void> props = new MapPropertyStore<>();
        assertNull(SimpleDurationUnit.SECONDS.loadValue(props));
    }

    // =====================================================================
    // defaultLoadValueMillis / defaultLoadValue
    // =====================================================================

    @Test
    void defaultLoadValueMillis_noMatch_returnsDefault() {
        MapPropertyStore<Void> props = new MapPropertyStore<>();
        assertEquals(9999L, SimpleDurationUnit.defaultLoadValueMillis(props, 9999L));
    }

    @Test
    void defaultLoadValueMillis_rawMs_parsedAsMillis() {
        MapPropertyStore<Void> props = new MapPropertyStore<>();
        props.putProperty(null, "5000");  // null property name = raw value
        assertEquals(5000L, SimpleDurationUnit.defaultLoadValueMillis(props, -1L));
    }

    @Test
    void defaultLoadValue_hoursProperty_returnsDuration() {
        MapPropertyStore<Void> props = new MapPropertyStore<>();
        props.putProperty("h", "1");
        assertEquals(Duration.ofHours(1), SimpleDurationUnit.defaultLoadValue(props, null));
    }

    @Test
    void defaultLoadValue_noMatch_returnsDefault() {
        MapPropertyStore<Void> props = new MapPropertyStore<>();
        Duration def = Duration.ofMinutes(5);
        assertEquals(def, SimpleDurationUnit.defaultLoadValue(props, def));
    }

}
