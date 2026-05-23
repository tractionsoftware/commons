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

import com.google.common.annotations.Beta;
import com.tractionsoftware.commons.properties.GetProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Beta
public enum SimpleDurationUnit {

    DAYS(TimeUnit.DAYS),

    HOURS(TimeUnit.HOURS),

    MINUTES(TimeUnit.MINUTES),

    SECONDS(TimeUnit.SECONDS),

    MILLISECONDS(TimeUnit.MILLISECONDS, "ms");

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleDurationUnit.class);

    private static final Pattern DURATION_PATTERN = Pattern.compile(
        "\\s*(([0-9]+)\\s*d)?" +
        "\\s*(([0-9]+)\\s*h)?" +
        "\\s*(([0-9]+)\\s*m)?" +
        "\\s*(([0-9]+)\\s*s)?" +
        "\\s*(([0-9]+)\\s*ms)?" +
        "\\s*"
    );

    public static final long defaultLoadValueMillis(GetProperty durationProps, long defaultValue) {
        for (SimpleDurationUnit p : values()) {
            long periodMs = p.loadValueMillis(durationProps);
            if (periodMs > 0) {
                return periodMs;
            }
        }
        long ms = durationProps.getLongProperty(null, Long.MIN_VALUE);
        if (ms != Long.MIN_VALUE) {
            return ms;
        }
        return defaultValue;
    }

    public static final Duration defaultLoadValue(GetProperty durationProps, Duration defaultValue) {
        for (SimpleDurationUnit p : values()) {
            Duration duration = p.loadValue(durationProps);
            if (duration != null) {
                return duration;
            }
        }
        long ms = durationProps.getLongProperty(null, Long.MIN_VALUE);
        if (ms != Long.MIN_VALUE) {
            return Duration.ofMillis(ms);
        }
        return defaultValue;
    }

    public static final Duration defaultParseValue(String value) {
        return defaultParseValue(value, null);
    }

    public static final Duration defaultParseValue(String value, Duration defaultValue) {

        value = StringUtils.trimToNull(value);
        if (value == null) {
            return defaultValue;
        }

        if ("0".equals(value)) {
            return Duration.ZERO;
        }

        try {
            return Duration.parse(value);
        }
        catch (DateTimeParseException e) {
            // Ignore
        }

        Matcher matcher = DURATION_PATTERN.matcher(value);
        try {
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Unrecognized duration format.");
            }
            return getTotalDuration(matcher);
        }
        catch (RuntimeException e) {
            LOGGER.warn("Failed to parse duration expression {}", value, e);
        }

        return defaultValue;

    }

    private static final Duration getTotalDuration(Matcher matcher) {
        Duration sum = Duration.ZERO;
        int g = 2;
        for (SimpleDurationUnit unit : SimpleDurationUnit.values()) {
            String v = matcher.group(g);
            if (v != null) {
                sum = sum.plus(unit.getDuration(Long.parseLong(v)));
            }
            g += 2;
        }
        return sum;
    }

    private final TimeUnit unit;

    private final String propName;

    private SimpleDurationUnit(TimeUnit unit) {
        this(unit, String.valueOf(Character.toLowerCase(unit.name().charAt(0))));
    }

    private SimpleDurationUnit(TimeUnit unit, String propName) {
        this.unit = unit;
        this.propName = propName;
    }

    public final long loadValueMillis(GetProperty properties) {
        long value = properties.getLongProperty(propName, Long.MIN_VALUE);
        if (value != Long.MIN_VALUE) {
            return unit.toMillis(value);
        }
        return 0;
    }

    public final Duration loadValue(GetProperty properties) {
        long millis = loadValueMillis(properties);
        if (millis == 0) {
            return null;
        }
        return Duration.ofMillis(millis);
    }

    public final TimeUnit getUnit() {
        return unit;
    }

    public final Duration getDuration(long value) {
        return Duration.of(value, unit.toChronoUnit());
    }

}
